from flask import Flask, request, jsonify
from flask_cors import CORS
import sqlite3

app = Flask(__name__)
CORS(app)  # Allow requests from JavaFX

# Simulated in-memory storage
users = {}
messages = {}
friendships = {}

# Initialize the database
def initialize_database():
    conn = sqlite3.connect('chat_app.db')
    cursor = conn.cursor()
    
    # Create Users table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL
    )
    ''')
    
    # Create Contacts table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Contacts (
        username TEXT NOT NULL,
        contact_username TEXT NOT NULL,
        FOREIGN KEY(username) REFERENCES Users(username)
    )
    ''')
    """ 
    # Create Chats table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Chats (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        message TEXT NOT NULL,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY(user_id) REFERENCES Users(id)
    )
    ''')"""
    
    conn.commit()
    conn.close()



@app.route("/register", methods=["POST"])
def register():
    conn = sqlite3.connect('chat_app.db')
    cursor = conn.cursor()
    data = request.json
    username = data.get("username")
    password = data.get("password")

    if not username or not password:
        return "Error:Username and password cannot be empty", 400
    if len(password) < 6:
        return "Error:Password must be at least 6 characters long", 400

    try:
        cursor.execute('INSERT INTO Users (username, password) VALUES (?, ?)', (username, password))
        conn.commit()
        print(f"User ({username}) added successfully.")
        return "Success:Your registration was successful. You can login now", 200
    except sqlite3.IntegrityError:
        print("Error: Username already exists.")
        return "Error:Username already exists", 400
    conn.close()

@app.route("/login", methods=["POST"])
def login():
    conn = sqlite3.connect('chat_app.db')
    cursor = conn.cursor()
    print("Received login request:", request.json)  # Debug log
    data = request.json
    if not data or "username" not in data or "password" not in data:
        return "Error:Invalid request format", 400

    username = data.get("username")
    password = data.get("password")

    cursor.execute('SELECT * FROM Users WHERE username = ? AND password = ?', (username, password))
    user = cursor.fetchone()

    conn.close()

    if user:
        return "Success:Login successful", 200
    else:
        return "Error:Invalid credentials", 400



@app.route("/ShowContacts", methods=["GET", 'POST'])
def ShowContacts():
    conn = sqlite3.connect('chat_app.db')
    cursor = conn.cursor()
    data = request.json
    username = data.get("username")
    cursor.execute('SELECT contact_username FROM Contacts WHERE username = ?', (username,))
    contacts = cursor.fetchall()

    print(contacts)
    print(contacts[0])

    conn.close();

    contacts_string = ";".join(contact[0] for contact in contacts)
    print(contacts_string)

    return contacts_string, 100

@app.route("/AddContacts", methods=["POST"])
def AddContacts():
    conn = sqlite3.connect('chat_app.db')
    cursor = conn.cursor()
    data = request.json
    username = data.get("username")
    contact_username = data.get("contact")
    cursor.execute('SELECT username FROM Users WHERE username = ?', (username,))
    user = cursor.fetchone()
    cursor.execute('SELECT username FROM Users WHERE username = ?', (contact_username,))
    contact = cursor.fetchone()
    cursor.execute('SELECT username FROM Contacts WHERE username = ? AND contact_username = ?', (username, contact_username,)) #Checks if the Username-ContactUsername combinations already exists
    exists = cursor.fetchone()

    if user and contact and (exists is None):  #exist is None is checking for connection above if it returns null which means we can add the contact cause it does not already exists 
        cursor.execute('INSERT INTO Contacts (username, contact_username) VALUES (?, ?)', (user[0], contact[0]))
        conn.commit()
        cursor.execute('INSERT INTO Contacts (username, contact_username) VALUES (?, ?)', (contact[0], user[0]))
        conn.commit()
        conn.close()
        return "Success:Contact added successfully.", 600
    else:
        conn.close()
        return "Error:User not found.", 601

    




if __name__ == "__main__":
    initialize_database()
    app.run(debug=True)
