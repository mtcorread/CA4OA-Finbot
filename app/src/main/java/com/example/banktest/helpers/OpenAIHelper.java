package com.example.banktest.helpers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIHelper {
    public static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient client;
    private final Handler handler;
    private final ExecutorService executorService;
    private final String apiKey;
    private String threadId;

    private static final String ASSISTANTS_URL = "https://api.openai.com/v1/assistants/";
    private static final String THREADS_URL = "https://api.openai.com/v1/threads";

    public OpenAIHelper(OkHttpClient client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
        this.executorService = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }
    public void makeChatGPTCall(String role, String prompt, String model, Consumer<String> onResponse) {
        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray messagesArray = new JSONArray();
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", role);
            messageObject.put("content", prompt);
            messagesArray.put(messageObject);
            jsonBody.put("model", model);
            jsonBody.put("messages", messagesArray);
            jsonBody.put("max_tokens", 2000);
            jsonBody.put("temperature", 1);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody.toString(), JSON);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postToMainThread(() -> onResponse.accept("Failed to load response due to " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("entró a chatgpt make call onResponse", "SI");

                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        Log.d("Generic GPT result", prompt + "; " + result);
                        postToMainThread(() -> onResponse.accept(result));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    postToMainThread(() -> {
                        try {
                            onResponse.accept("Failed to load response due to " + response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            response.close();
                        }
                    });
                }
            }
        });
    }

    public void retrieveAndCreate(String assistantId, Runnable onComplete) {
        new Thread(() -> {
            try {
                Log.d("OpenAIHelper", "Starting retrieveAndCreate");
                String assistantDetails = retrieveAssistant(assistantId);
                Log.d("OpenAIHelper", "Assistant Details: " + assistantDetails);
                String threadResponse = createThread();
                Log.d("OpenAIHelper", "Thread Response: " + threadResponse);
                Log.d("OpenAIHelper", "Thread ID: " + threadId);
            } catch (Exception e) {
                Log.e("OpenAIHelper", "Error during retrieveAndCreate", e);
            } finally {
                postToMainThread(onComplete);
            }
        }).start();
    }

    private String retrieveAssistant(String assistantId) {
        Request request = createGetRequest(ASSISTANTS_URL + assistantId);
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                Log.d("OpenAIHelper", "retrieveAssistant response body: " + responseBody);
                return responseBody;
            } else {
                String errorMsg = response.body() != null ? response.body().string() : "No response body";
                Log.e("OpenAIHelper", "Failed to retrieve assistant: " + response.message() + ", " + errorMsg);
                return "Failed to retrieve assistant: " + response.message() + ", " + errorMsg;
            }
        } catch (Exception e) {
            Log.e("OpenAIHelper", "Error in retrieveAssistant", e);
            return "Error: " + e.getMessage();        }
    }

    public String createThread() {
        RequestBody body = RequestBody.create("", JSON);
        Request request = createPostRequest(THREADS_URL, body);

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                Log.d("OpenAIHelper", "createThread response body: " + responseBody);
                threadId = new JSONObject(responseBody).getString("id"); // Extract and save the thread ID
                return responseBody;
            } else {
                String errorMsg = response.body() != null ? response.body().string() : "No response body";
                Log.e("OpenAIHelper", "Failed to create thread: " + response.message() + ", " + errorMsg);
                return "Failed to create thread: " + response.message() + ", " + errorMsg;
            }
        } catch (Exception e) {
            Log.e("OpenAIHelper", "Error in createThread", e);
            return "Error: " + e.getMessage();
        }
    }

    public String getThreadId() {
        return threadId;
    }


    public void sendMessage(String question, String instr, Float temp, Consumer<String> onResponse) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("role", "user");
            jsonBody.put("content", question);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = createPostRequest("https://api.openai.com/v1/threads/" + threadId + "/messages", body);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postToMainThread(() -> onResponse.accept("Failed to load response due to " + e.getMessage()));
                Log.e("sendMessage", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        initiateRun(threadId, instr, temp, onResponse);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        response.close();
                    }
                } else {
                    postToMainThread(() -> {
                        try {
                            onResponse.accept("Failed to load response due to " + response.body().string());
                            Log.e("sendMessage", "onResponse unsuccessful: " + response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            response.close();
                        }
                    });
                }
            }
        });
    }

    private void initiateRun(String threadId, String instr, Float temp, Consumer<String> onResponse) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("assistant_id", "asst_OJZkZLb9OiMTZrflC4HES2tr");
            jsonBody.put("instructions", instr);
            jsonBody.put("temperature", temp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Log.d("bodyRUN", jsonBody.toString());
        Request runRequest = createPostRequest("https://api.openai.com/v1/threads/" + threadId + "/runs", body);

        client.newCall(runRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postToMainThread(() -> onResponse.accept("Failed to start run due to: " + e.getMessage()));
                Log.e("initiateRun", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    try {
                        String errorMessage = response.body() != null ? response.body().string() : "No error message provided";
                        postToMainThread(() -> onResponse.accept("Failed to start run due to: " + errorMessage));
                        Log.e("initiateRun", "onResponse unsuccessful: " + errorMessage);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        response.close();
                    }
                } else {
                    checkRunStatus(threadId, onResponse);
                    //handler.postDelayed(() -> retrieveMessages(threadId, onResponse), 10000);

                }
            }
        });
    }

    private void checkRunStatus(String threadId, Consumer<String> onResponse) {
        String url = "https://api.openai.com/v1/threads/" + threadId + "/runs";
        Request request = createGetRequest(url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postToMainThread(() -> onResponse.accept("Failed to check run status due to: " + e.getMessage()));
                Log.e("checkRunStatus", "onFailure: ", e); // Log full stack trace
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "null";
                    postToMainThread(() -> onResponse.accept("Failed to check run status due to: " + response.message() + " (HTTP " + response.code() + ")"));
                    Log.e("checkRunStatus", "onResponse unsuccessful: " + response.message() + " (HTTP " + response.code() + "), Response Body: " + responseBody);
                } else {
                    try {
                        String responseData = response.body().string();
                        Log.d("checkRunStatus", "Response Data: " + responseData); // Debug log for response data
                        JSONObject responseObject = new JSONObject(responseData);
                        JSONArray runsArray = responseObject.getJSONArray("data");

                        if (runsArray.length() > 0) {
                            JSONObject firstRun = runsArray.getJSONObject(0);
                            String status = firstRun.getString("status");
                            Log.d("checkRunStatus", "Run Status: " + status); // Debug log for run status
                            if ("completed".equals(status)) {
                                retrieveMessages(threadId, onResponse);
                            } else {
                                handler.postDelayed(() -> checkRunStatus(threadId, onResponse), 1000);
                            }
                        } else {
                            postToMainThread(() -> onResponse.accept("No runs found for the provided thread ID."));
                            Log.e("checkRunStatus", "No runs found for the provided thread ID.");
                        }
                    } catch (JSONException e) {
                        postToMainThread(() -> onResponse.accept("Failed to parse run status: " + e.getMessage()));
                        Log.e("checkRunStatus", "JSONException: ", e); // Log full stack trace
                    } finally {
                        response.close();
                    }
                }
            }
        });
    }

    private void retrieveMessages(String threadId, Consumer<String> onResponse) {
        Request messageRequest = createGetRequest("https://api.openai.com/v1/threads/" + threadId + "/messages");

        client.newCall(messageRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postToMainThread(() -> onResponse.accept("Failed to retrieve messages due to: " + e.getMessage()));
                Log.e("retrieveMessages", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    postToMainThread(() -> onResponse.accept("Failed to retrieve messages due to: " + response.message()));
                    Log.e("retrieveMessages", "onResponse unsuccessful: " + response.message());
                } else {
                    try {
                        String responseData = response.body().string();
                        JSONObject responseObject = new JSONObject(responseData);
                        JSONArray messagesArray = responseObject.getJSONArray("data");

                        if (messagesArray.length() > 0) {
                            JSONObject firstMessage = messagesArray.getJSONObject(0);
                            JSONObject contentObject = firstMessage.getJSONArray("content").getJSONObject(0);
                            JSONObject textObject = contentObject.getJSONObject("text");
                            String messageText = textObject.getString("value");
                            postToMainThread(() -> onResponse.accept(messageText));
                        }
                    } catch (JSONException e) {
                        postToMainThread(() -> onResponse.accept("Failed to parse message content."));
                        Log.e("retrieveMessages", "JSONException: " + e.getMessage());
                    } finally {
                        response.close();
                    }
                }
            }
        });
    }

    private Request createPostRequest(String url, RequestBody body) {
        return new Request.Builder()
                .url(url)
                .header("OpenAI-Beta", "assistants=v2")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();
    }

    private Request createGetRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("OpenAI-Beta", "assistants=v2")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .build();
    }
    private void postToMainThread(Runnable action) {
        handler.post(action);
    }


}
