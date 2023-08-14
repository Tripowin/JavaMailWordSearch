class EmailSearcher:
    def __init__(self, host, port, username, password, search_subject):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.search_subject = search_subject

    def search_emails(self):
        try:
            with imaplib.IMAP4_SSL(self.host, self.port) as mail:
                mail.login(self.username, self.password)
                mail.select("inbox")
                result, message_numbers = mail.search(None, f'SUBJECT "{self.search_subject}"')
                
                if result == "OK":
                    message_numbers = message_numbers[0].split()
                    emails = []
                    for num in message_numbers:
                        result, data = mail.fetch(num, "(RFC822)")
                        raw_email = data[0][1]
                        msg = email.message_from_bytes(raw_email)
                        emails.append(msg)
                    return emails
                else:
                    print("Error searching emails:", result)
        except Exception as e:
            print("Error:", e)

    def save_emails_to_database(self, emails):
        try:
            connection = pymysql.connect(
                host="localhost",
                user="usuario",
                password="password",
                database="challenge",
                cursorclass=pymysql.cursors.DictCursor
            )
            with connection.cursor() as cursor:
                for msg in emails:
                    subject = msg["subject"]
                    date_str = msg["date"]
                    date = datetime.strptime(date_str, "%a, %d %b %Y %H:%M:%S %z")
                    from_ = decode_header(msg["from"])[0][0]
                    cursor.execute(
                        "INSERT INTO mails (Fecha, FromAddress, Subject) VALUES (%s, %s, %s)",
                        (date, from_, subject)
                    )
                connection.commit()
        except Exception as e:
            print("Error:", e)
        finally:
            if connection:
                connection.close()

def main():
    host = "imap.gmail.com"
    port = 993
    username = "tucorreo@gmail.com"
    password = "tupassword"
    search_subject = "DevOps"
    
    email_searcher = EmailSearcher(host, port, username, password, search_subject)
    emails = email_searcher.search_emails()
    
    if emails:
        email_searcher.save_emails_to_database(emails)
        print("Emails saved to database.")

if __name__ == "__main__":
    main()