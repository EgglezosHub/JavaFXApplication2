<p align="center">
  <img src="https://github.com/EgglezosHub/JavaFXApplication2/blob/main/resourses/🗫%C2%A0ChatterBox.png">
</p>

<h1 align="center">Messaging-App (In Progress)</h1>

<p align="center">
  <img src="https://img.shields.io/github/created-at/EgglezosHub/JavaFXApplication2?color=dark%20green">
  <img src="https://img.shields.io/github/contributors/EgglezosHub/JavaFXApplication2?color=dark%20green">
  <img src="https://img.shields.io/github/languages/count/EgglezosHub/JavaFXApplication2?color=dark%20green">
  <img src="https://img.shields.io/github/languages/top/EgglezosHub/JavaFXApplication2?color=dark%20green">
</p>



A simple Messaging App that I developed to practice my knowledge on:
- Java FX
- Python
- Database Connectivity
- Encryption with C++

## Key features
- Creating an account
- Add other users from the database
- Exchange messages with your contacts

## Usage

- After running the App:
  - If you already have an account
    - Login using your credentials
  - If you use the app for the first time 
    - Register by pressing the "Register" button
    - Create a unique username and a secure password
    - Press the "Back to Login" button to return to Login Page
    - Login using your newly created credentials

![alt text](https://github.com/EgglezosHub/JavaFXApplication2/blob/e37f1108bf8d4438d701cdce7293c80b1bb66029/resourses/login.gif)

- After connecting to your account you can:
  - Add other users using the search bar on the top left (make sure to type the correct username)
  - Select contacts
  - Send messages 

![alt text](https://github.com/EgglezosHub/JavaFXApplication2/blob/e37f1108bf8d4438d701cdce7293c80b1bb66029/resourses/Chat.gif)


## Installation
### 1. Download Java and Python
<br/>

Navigate to the project folder in bash
  - Java: 
```bash
sudo apt update
sudo apt install openjdk-21-jdk

```
  - Python:
```bash
sudo apt update
sudo apt install python3
```
### 2. Download Flask and Flask-Cors  

Install pip

```bash
  sudo apt install python3-pip
```

Create a virtual environment

```bash
   sudo apt install python3-venv
   python3 -m venv env_test
   source env_test/bin/activate
```
Install Flask
```bash
   pip install Flask
```
Install Flask-Cors
```bash
   pip install Flask-Cors
```
### 3. Download JavaFX (Same version as Java)
  - Download JavaFX from [GLUON](https://gluonhq.com/products/javafx/)
  - Extract the zip
  - Copy the path to the lib folder
  - Write the following command with `Path_to_javafx-sdk-23.0.2/lib` beeing the path to the **lib** folder of the JavaFX extracted zip file
```bash
  export PATH_TO_FX=Path_to_javafx-sdk-23.0.2/lib
```
### 4. Download GSON
```bash
wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar -P ~/Desktop/JavaFXApplication2/lib/
```
- `~/Desktop/JavaFXApplication2/lib/` : location of cloned project

### 5. Compile & Run
```bash
cd src
```
### Compile
```bash
javac --module-path /home/user/Downloads/openjfx-23.0.2_linux-x64_bin-sdk/javafx-sdk-23.0.2/lib
--add-modules javafx.controls,javafx.fxml,javafx.media
-cp ~/Desktop/JavaFXApplication2/lib/gson-2.10.1.jar
javafxapplication2/*.java
```
- `/home/user/Downloads/openjfx-23.0.2_linux-x64_bin-sdk/javafx-sdk-23.0.2/lib` :
  - Exact Path to the downloaded (and extracted) Javafx File

- `~/Desktop/JavaFXApplication2/lib/gson-2.10.1.jar` :
  - Exact Path to the downloaded gson jar

### Run
```bash
java --module-path /home/user/Downloads/openjfx-23.0.2_linux-x64_bin-sdk/javafx-sdk-23.0.2/lib \
--add-modules javafx.controls,javafx.fxml,javafx.media \
-cp ".:/home/user/Desktop/JavaFXApplication2/lib/gson-2.10.1.jar" \
javafxapplication2.JavaFXApplication2

```






