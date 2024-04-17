import sqlite3
import random
import time

def create_database():
    conn = sqlite3.connect('dyslexiadetect.db')
    conn.row_factory = sqlite3.Row  
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY,
            email TEXT UNIQUE,
            name TEXT,
            username TEXT UNIQUE,
            age INTEGER,
            password TEXT,
            is_verified INTEGER DEFAULT 0
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS otps (
            email TEXT PRIMARY KEY,
            otp TEXT,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS rhyming_words (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            ComplexityLevel INTEGER NOT NULL,
            Word TEXT NOT NULL,
            Rhyme1 TEXT NOT NULL,
            Rhyme2 TEXT NOT NULL,
            NonRhyme1 TEXT NOT NULL,
            NonRhyme2 TEXT NOT NULL,
            NonRhyme3 TEXT NOT NULL
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS screening_paragraphs (
            id INTEGER PRIMARY KEY,
            age_group TEXT NOT NULL,
            paragraph TEXT NOT NULL,
            word_count INTEGER
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS word_harmony (
                ComplexityLevel INTEGER CHECK(ComplexityLevel BETWEEN 1 AND 3),
                Type TEXT CHECK(Type IN ('letter', 'word')),
                Word TEXT,
                SoundPath TEXT,
                PRIMARY KEY(Word, ComplexityLevel)
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS activities (
                activity_id INTEGER PRIMARY KEY AUTOINCREMENT,
                activity_name TEXT NOT NULL
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS levels (
                level_id INTEGER PRIMARY KEY AUTOINCREMENT,
                activity_id INTEGER NOT NULL,
                level_number INTEGER NOT NULL,
                FOREIGN KEY (activity_id) REFERENCES Activities(activity_id)
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS scores (
                CREATE TABLE IF NOT EXISTS scores (
                score_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                level_id INTEGER NOT NULL,
                score INTEGER NOT NULL,
                date_attempted DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (level_id) REFERENCES Levels(level_id)
        )
    ''')
                   

    conn.commit()
    conn.close()


########## Users ##########
def generate_sequential_user_id(cursor):
    # Find the maximum id value in the users table
    cursor.execute("SELECT MAX(id) FROM users")
    max_id = cursor.fetchone()[0]
    if max_id is None:
        # If the table is empty, start from 1000
        return 1000
    else:
        # Otherwise, add 1 to the maximum id to get the next id
        return max_id + 1
    
def add_user(email, name, username, age, password):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    # Generate a sequential user ID
    user_id = generate_sequential_user_id(cursor)
    try:
        cursor.execute('INSERT INTO users (id, email, name, username, age, password) VALUES (?, ?, ?, ?, ?, ?)', 
                       (user_id, email, name, username, age, password))
        conn.commit()
    except sqlite3.IntegrityError as e:
        print(f"Integrity Error: {e}")  # Log the error
        return False
    except Exception as e:
        print(f"General Error: {e}")  # Log any other error
        return False
    finally:
        conn.close()
    return True





    
def store_otp(email, otp):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    cursor.execute('REPLACE INTO otps (email, otp) VALUES (?, ?)', (email, otp))
    conn.commit()
    conn.close()

def verify_user(email, otp):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    cursor.execute("SELECT otp FROM otps WHERE email = ? AND datetime(timestamp, '+10 minutes') > CURRENT_TIMESTAMP", (email,))
    stored_otp = cursor.fetchone()
    if stored_otp and stored_otp[0] == otp:
        cursor.execute('UPDATE users SET is_verified = 1 WHERE email = ?', (email,))
        conn.commit()
        conn.close()
        return True
    conn.close()
    return False

# def authenticate_user(email, hashed_password):
#     # Connect to your database
#     conn = sqlite3.connect('dyslexiadetect.db')
#     cursor = conn.cursor()

#     # Query the database for the user with the given email and password
#     cursor.execute("SELECT id FROM users WHERE email = ? AND password = ?", (email, hashed_password))
#     user = cursor.fetchone()
    
#     # Close the connection
#     conn.close()

#     if user:
#         # Return the user ID if authentication is successful
#         return user[0]
#     else:
#         # Return None or False if authentication fails
#         return None
def authenticate_user(email, password):
    """
    Authenticate a user based on their email and password.

    Parameters:
    - email (str): The email of the user.
    - password (str): The password provided by the user, already hashed.

    Returns:
    - tuple: A tuple containing the user ID and a boolean indicating authentication success.
    """
    # Connect to the database
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()

    try:
        # Query to fetch the user's ID, hashed password, and verification status
        cursor.execute('SELECT id, password, is_verified FROM users WHERE email = ?', (email,))
        user = cursor.fetchone()

        # Check if user exists, the passwords match, and the user is verified
        if user and user[1] == password and user[2] == 1:
            return user[0], True  # User authenticated successfully
        else:
            return None, False  # Authentication failed
    except Exception as e:
        # Log the exception if something goes wrong
        print(f"Error during authentication: {e}")
        return None, False
    finally:
        # Always close the connection
        conn.close()

from datetime import datetime


def store_user_token(email, user_id, token):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        # Use REPLACE to update the token if the user_id already exists
        cursor.execute('REPLACE INTO user_tokens (email, user_id, token, timestamp) VALUES (?, ?, ?, ?)', 
                       (email, user_id, token, datetime.now()))
        conn.commit()
    except Exception as e:
        print(f"Error storing user token: {e}")
    finally:
        conn.close()

########## Paragraphs ##########

def add_paragraph(age_group, paragraph):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        cursor.execute('INSERT INTO screening_paragraphs (age_group, paragraph) VALUES (?, ?)',
                       (age_group, paragraph))
        conn.commit()
    except sqlite3.IntegrityError as e:
        print(f"Integrity Error: {e}")
        return False
    except Exception as e:
        print(f"General Error: {e}")
        return False
    finally:
        conn.close()
    return True

def get_paragraph_initial(age_group):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        cursor.execute('SELECT paragraph, word_count FROM paragraphs WHERE age_group = ? ORDER BY RANDOM() LIMIT 1', (age_group,))
        result = cursor.fetchone()
        return (result[0], result[1]) if result else (None, None)
    except Exception as e:
        print(f"Error in getting paragraph: {e}")
        return None, None
    finally:
        conn.close()


import sqlite3

def get_paragraph(age_group):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        cursor.execute('SELECT paragraph, word_count , theme FROM paragraphs WHERE age_group = ? ORDER BY RANDOM() LIMIT 1', (age_group,))
        result = cursor.fetchone()
        if result:
            return result  # Returning both paragraph and word_count
        else:
            print(f"No paragraph found for age group: {age_group}")
            return None, None  # Return None for both paragraph and word_count
    except sqlite3.Error as e:
        print(f"Error in getting paragraph: {e}")
        return None, None  # Return None for both paragraph and word_count
    finally:
        conn.close()
    







######### Rhyming Activity ##########


def get_rhyming_task(level):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()

    query = """
    SELECT Word, Rhyme1, NonRhyme1, NonRhyme2, NonRhyme3 
    FROM rhyming_words 
    WHERE ComplexityLevel = ? 
    ORDER BY RANDOM() 
    LIMIT 1
    """
    cursor.execute(query, (level,))
    task = cursor.fetchone()

    if task:
        # Include one rhyming word and three non-rhyming words in options
        options = [task[1], task[2], task[3], task[4]]
        random.shuffle(options)
        print(f"Fetched task: Word - {task[0]}, Options - {options}")
        return task[0], options
    else:
        print("No task found for the given level.")
        return None, None



def validate_rhyming_answer(word, chosen_option):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    cursor.execute("SELECT Rhyme1, Rhyme2 FROM rhyming_words WHERE Word = ?", (word,))
    correct_answers = cursor.fetchone()
    conn.close()

    return chosen_option in correct_answers


# def update_user_score(user_id, level, score):
#     """
#     Update or insert the user's score for a given level.
#     """
#     conn = sqlite3.connect('dyslexiadetect.db')
#     cursor = conn.cursor()

#     # Check if a score for the level already exists
#     cursor.execute('''
#         SELECT score_id FROM scores
#         WHERE user_id = ? AND level_id = ?
#     ''', (user_id, level))

#     result = cursor.fetchone()

#     if result:
#         # If score exists, update it
#         score_id = result[0]
#         cursor.execute('''
#             UPDATE scores
#             SET score = ?, date_attempted = CURRENT_TIMESTAMP
#             WHERE score_id = ?
#         ''', (score, score_id))
#     else:
#         # If no existing score, insert new record
#         cursor.execute('''
#             INSERT INTO scores (user_id, level_id, score)
#             VALUES (?, ?, ?)
#         ''', (user_id, level, score))

#     conn.commit()
#     conn.close()



def update_user_score(user_id, level, score):
    """
    Inserts the user's score for a given level, tracking every attempt.
    """
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    
    # Insert new record for each attempt
    cursor.execute('''
        INSERT INTO scores (user_id, level_id, score, date_attempted)
        VALUES (?, ?, ?, CURRENT_TIMESTAMP)
    ''', (user_id, level, score))
    
    conn.commit()
    conn.close()




######### Word Harmony Activity ##########


def get_word_harmony_task(level):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()

    type_of_task = 'letter' if level == 1 else 'word'
    correct_query = """
    SELECT Word, SoundPath 
    FROM word_harmony 
    WHERE ComplexityLevel = ? AND Type = ?
    ORDER BY RANDOM() 
    LIMIT 1
    """
    incorrect_query = """
    SELECT Word 
    FROM word_harmony 
    WHERE ComplexityLevel = ? AND Type = ? AND Word != ?
    ORDER BY RANDOM() 
    LIMIT 5
    """
    
    cursor.execute(correct_query, (level, type_of_task))
    correct_task = cursor.fetchone()

    if correct_task:
        correct_word, sound_path = correct_task
        cursor.execute(incorrect_query, (level, type_of_task, correct_word))
        incorrect_words = [row[0] for row in cursor.fetchall()]
        options = [correct_word] + incorrect_words
        random.shuffle(options)  # Shuffle to randomize the position of the correct word
        return correct_word, sound_path, options
    else:
        return None, None, None



def validate_word_harmony_answer(word, chosen_word):
    # This function assumes that the correct answer is directly provided for comparison
    return word == chosen_word


def safe_execute(db_path, query, params=None, max_attempts=5):
    params = params or []
    attempt = 0
    while attempt < max_attempts:
        try:
            with sqlite3.connect(db_path) as conn:
                cursor = conn.cursor()
                cursor.execute(query, params)
                if query.strip().upper().startswith("SELECT"):
                    return cursor.fetchall()  # Return query result for SELECT statements
                conn.commit()
                return True  # Success for INSERT/UPDATE/DELETE
        except sqlite3.OperationalError as e:
            if "locked" in str(e):
                attempt += 1
                time.sleep(0.1)  # Wait a bit before retrying
            else:
                raise  # Re-raise unexpected errors
    return False  # Failed after max_attempts for INSERT/UPDATE/DELETE or None for SELECT

def insert_word_harmony_score(user_id, activity_name, level_number, score):
    db_path = 'dyslexiadetect.db'

    # Get activity_id based on the activity_name
    activity_id_result = safe_execute(db_path, "SELECT activity_id FROM activities WHERE activity_name = ?", (activity_name,))
    if not activity_id_result:
        print("Failed to get activity_id")
        return False
    activity_id = activity_id_result[0][0]

    # Get level_id based on the activity_id and level_number
    level_id_result = safe_execute(db_path, "SELECT level_id FROM levels WHERE activity_id = ? AND level_number = ?", (activity_id, level_number))
    if not level_id_result:
        print("Failed to get level_id")
        return False
    level_id = level_id_result[0][0]

    # Insert the score
    success = safe_execute(db_path, "INSERT INTO scores (user_id, level_id, score, date_attempted) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", (user_id, level_id, score))
    if not success:
        print("Failed to insert score")
        return False

    return True


######### Audio Rhyme Selection Activity ##########


#def get_audio_rhyme_word_and_options(level):
    # """
    # Fetches a random word and options from the rhyming_words table based on the specified level.
    # """
    # conn = sqlite3.connect('dyslexiadetect.db')
    # cursor = conn.cursor()
    # cursor.execute("""
    #     SELECT Word, Rhyme1, Rhyme2, NonRhyme1, NonRhyme2, NonRhyme3
    #     FROM rhyming_words
    #     WHERE ComplexityLevel = ?
    #     ORDER BY RANDOM()
    #     LIMIT 1
    # """, (level,))
    # task = cursor.fetchone()
    # conn.close()

    # if task:
    #     word, rhyme1, rhyme2, non_rhyme1, non_rhyme2, non_rhyme3 = task
    #     # Randomly select between Rhyme1 and Rhyme2
    #     correct_rhyme = random.choice([rhyme1, rhyme2])
    #     options = [correct_rhyme, non_rhyme1, non_rhyme2, non_rhyme3]
    #     random.shuffle(options)
    #     return word, options
    # else:
    #     return None, []



def get_audio_rhyme_word_and_options(level):
    """
    Fetches a random word and options from the rhyming_words table based on the specified level.
    For levels 1 and 2, words from ComplexityLevel 2 are included.
    For level 3, words from ComplexityLevel 3 are used.
    """
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    
    # Adjust the query based on the level
    if level in [1, 2]:
        query_levels = (2,)  # Use ComplexityLevel 2 words for levels 1 and 2
    else:
        query_levels = (3,)  # Use ComplexityLevel 3 words for level 3

    cursor.execute("""
        SELECT Word, Rhyme1, Rhyme2, NonRhyme1, NonRhyme2, NonRhyme3
        FROM rhyming_words
        WHERE ComplexityLevel = ?
        ORDER BY RANDOM()
        LIMIT 1
    """, query_levels)
    task = cursor.fetchone()
    conn.close()

    if task:
        word, rhyme1, rhyme2, non_rhyme1, non_rhyme2, non_rhyme3 = task
        # Randomly select between Rhyme1 and Rhyme2 for the correct rhyme
        correct_rhyme = random.choice([rhyme1, rhyme2])
        # Shuffle the options so the correct answer's position varies
        options = [correct_rhyme, non_rhyme1, non_rhyme2, non_rhyme3]
        random.shuffle(options)
        return word, options
    else:
        return None, []


def check_audio_rhyme(word, user_response):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT Rhyme1, Rhyme2 FROM rhyming_words WHERE Word = ?", (word,))
        rhymes = cursor.fetchone()
        if not rhymes:  # Add a check to see if the query returned a result
            return False

        # Filter out None values and convert to lowercase
        rhymes_list = [rhyme.lower() for rhyme in rhymes if rhyme is not None]

        # Check if the user response is in the list of valid rhymes
        return user_response.lower() in rhymes_list
    finally:
        conn.close()


def insert_audio_rhyme_score(user_id, level, score):
    with sqlite3.connect('dyslexiadetect.db') as conn:
        cursor = conn.cursor()

        # Assuming 'Audio Rhyme' is the name of the activity in your 'activities' table
        activity_name = 'Audio Rhyme'

        cursor.execute("SELECT activity_id FROM activities WHERE activity_name = ?", (activity_name,))
        activity_id = cursor.fetchone()[0]

        cursor.execute("SELECT level_id FROM levels WHERE activity_id = ? AND level_number = ?", (activity_id, level))
        level_id = cursor.fetchone()[0]

        cursor.execute("INSERT INTO scores (user_id, level_id, score, date_attempted) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", (user_id, level_id, score))

        conn.commit()
    return True



######### Scores ##########



def get_all_attempts_for_progress_tracking(user_id):
    """
    Fetches scores for all activities for the given user ID,
    and groups them by activities and levels.
    """
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT a.activity_name, l.level_number, s.score
        FROM scores s
        JOIN levels l ON s.level_id = l.level_id
        JOIN activities a ON l.activity_id = a.activity_id
        WHERE s.user_id = ?
        ORDER BY a.activity_id, l.level_number
    """, (user_id,))
    
    scores = cursor.fetchall()
    conn.close()

    # Structure to hold the formatted scores
    formatted_scores = []
    current_activity = {}
    
    for row in scores:
        activity_name, level_number, score = row
        
        # Check if we're still on the same activity
        if current_activity.get("activity_name") != activity_name:
            # If not, start a new activity and add it to the list
            if current_activity:
                # If there's a current activity, add it to the list before resetting
                formatted_scores.append(current_activity)
            
            current_activity = {
                "activity_name": activity_name,
                "levels": []
            }
        
        # Add the current level and score to the current activity
        current_activity["levels"].append({
            "level_number": level_number,
            "score": score
        })
    
    # Don't forget to add the last activity to the list
    if current_activity:
        formatted_scores.append(current_activity)

    return formatted_scores


def get_max_scores_by_level(user_id):
    """
    Fetches the maximum scores for each level of each activity for the given user ID.
    """
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()

    cursor.execute("""
        SELECT a.activity_name, l.level_number, MAX(s.score) AS max_score
        FROM scores s
        JOIN levels l ON s.level_id = l.level_id
        JOIN activities a ON l.activity_id = a.activity_id
        WHERE s.user_id = ?
        GROUP BY a.activity_name, l.level_number
        ORDER BY a.activity_id, l.level_number
    """, (user_id,))

    scores = cursor.fetchall()
    conn.close()

    formatted_scores = []
    current_activity = {}

    for activity_name, level_number, max_score in scores:
        if current_activity.get("activity_name") != activity_name:
            if current_activity:
                formatted_scores.append(current_activity)
            current_activity = {"activity_name": activity_name, "levels": []}

        current_activity["levels"].append({
            "level_number": level_number,
            "max_score": max_score
        })

    if current_activity:
        formatted_scores.append(current_activity)

    return formatted_scores


######## Progress Tracking ##########

def get_user_progress(user_id):
    """
    Fetches all attempts for all levels of activities played by a specific user.
    """
    with sqlite3.connect('dyslexiadetect.db') as conn:
        cursor = conn.cursor()
        cursor.execute('''
        SELECT a.activity_name, l.level_number, s.score, s.date_attempted
        FROM scores s
        JOIN levels l ON s.level_id = l.level_id
        JOIN activities a ON l.activity_id = a.activity_id
        WHERE s.user_id = ?
        ORDER BY a.activity_id, l.level_number, s.date_attempted
        ''', (user_id,))
        progress_data = cursor.fetchall()
    return progress_data




######### User Profile ##########

def get_user_profile(user_id):
    conn = sqlite3.connect('dyslexiadetect.db')
    cursor = conn.cursor()
    cursor.execute("SELECT id, email, name, username, age FROM users WHERE id=?", (user_id,))
    profile = cursor.fetchone()
    conn.close()
    if profile:
        # Adjust according to your users table structure
        return {"id": profile[0], "email": profile[1], "name": profile[2], "username": profile[3], "age": profile[4]}
    return None

def update_user_profile(user_id, name, username, age, old_password, new_password):
    try:
        conn = sqlite3.connect('dyslexiadetect.db')
        cursor = conn.cursor()
        
        # Assuming you are storing passwords as hashes and comparing old passwords for security
        cursor.execute("SELECT password FROM users WHERE id=?", (user_id,))
        current_password_hash = cursor.fetchone()[0]
        
        # Verify old_password here before updating
        
        cursor.execute("UPDATE users SET name=?, username=?, age=? WHERE id=?", (name, username, age, user_id))
        conn.commit()
        
        # Update password if necessary
        if new_password:
            cursor.execute("UPDATE users SET password=? WHERE id=?", (new_password, user_id))
            conn.commit()
        
        return True
    except Exception as e:
        print(f"Database error: {e}")
        return False
    finally:
        conn.close()


def upload_profile_picture(user_id, image_data):
    try:
        conn = sqlite3.connect('dyslexiadetect.db')
        cursor = conn.cursor()
        cursor.execute("UPDATE users SET profile_picture = ? WHERE id = ?", (image_data, user_id))
        conn.commit()
        conn.close()
        return {'message': 'Profile picture uploaded successfully'}, 200
    except Exception as e:
        return {'error': str(e)}, 500

def fetch_profile_picture(user_id):
    try:
        conn = sqlite3.connect('dyslexiadetect.db')
        cursor = conn.cursor()
        cursor.execute("SELECT profile_picture FROM users WHERE id = ?", (user_id,))
        image_data = cursor.fetchone()
        conn.close()
        if image_data:
            return image_data[0]
        else:
            return None
    except Exception as e:
        print(str(e))
        return None

    
# Ensure to call create_database() to initialize your database if it's not already created.

if __name__ == '__main__':
    create_database()