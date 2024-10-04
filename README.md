# 💰 CA4OA - FinBot
This project aims to test the acceptance of Fintech and AI among older adults. 
Please note that the app is not connected to any real bank, and all details and transactions within the app are purely simulated.

## Features  
- 💬 **AI-Powered Chat**: Get instant responses to banking-related questions.
- 🔐 **Secure Transactions**: View balances and manage transactions safely.
- 🌙 **Dark/Light Mode**: Customize the app’s look for your comfort.
- 🔊 **Voice & Text Support**: Interact using voice commands or text.

## Development Environment and Tools  

### 🖥️ Operating System: Android  
FinBot is built for Android, offering broad device compatibility and access to notifications, multimedia, and other advanced features.

### 💻 Programming Language: Java  
Written in Java, the app benefits from platform independence, strong object-oriented capabilities, and modular design.


## Architecture: 🏗️ Model-View-ViewModel (MVVM)  
**MVVM** separates the user interface from business logic for better maintainability.

- **Model**: Manages data for transactions and user queries.
- **ViewModel**: Handles input and state management.
- **View**: Displays chat and manages user interactions.

## APIs  
**OpenAI API** enables FinBot’s chat functionality:
- **Sending Messages**: Processes user queries.
- **Initiating Runs**: Starts new conversation threads.
- **Retrieving Messages**: Fetches AI-generated responses.

## Chat Functionality  
- 📖 **Text Segmentation**: Breaks long responses into smaller chunks for readability and accessibility.
- 🗣️ **Input Handling**: Processes both voice and text inputs efficiently.

## Transfer Page  
The **Transfer Page** simplifies fund transfers with conversational AI and **Text-to-Speech (TTS)**.

- 🗣️ **TTS Initialization**: Sets up speech capabilities.
- 🤖 **Bot Responses**: Guides users through account details and confirmation steps.
- 📲 **Input Handling**: Adjusts input requirements dynamically based on conversation stage.

## Balance Page  
The **Balance Page** shows current balances, transactions, and upcoming payments. Users can click on items for detailed views.

## Accessibility and Personalisation  
- 🌙 **Dark/Light Mode**: Customize themes for better visibility.
- 🔠 **Text Size Adjustment**: Modify text sizes for readability.
- 🎙️ **TTS Voice Assistant**: Reads out chat messages for enhanced accessibility.

## 🚀 How to Use

1. **🔑 Set Up the API Key**  
   - Open `ChatViewModel.java` file.
   - Replace the placeholder with your OpenAI API key.

2. **📲 Install the App**
   - Clone the repository and open the project in Android Studio.
   - Build and run the app on an Android device or emulator.
