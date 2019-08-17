import os
import sys
import django
from django.db import connection
import logging
import datetime

sys.path.insert(0, os.path.realpath(''))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger("Import initial database")
cur = connection.cursor()

logger.info("Import group, user, user_group")
filename = 'databases/auth_import.sql'
file = open(filename, 'r')
sql = " ".join(file.readlines())
logger.info("Start executing: {} at {}".format(filename, str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M"))))
cur.execute(sql)

logger.info("Import information of book")
filename_title = 'databases/title.sql'
file_title = open(filename_title, 'r')
sql_title = " ".join(file_title.readlines())
logger.info("Start executing: {} at {}".format(filename_title, str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M"))))
cur.execute(sql_title)

logger.info("Import barcode of book")
filename_books = 'databases/books.sql'
file_books = open(filename_books, 'r')
sql_books = " ".join(file_books.readlines())
logger.info("Start executing: {} at {}".format(filename_books, str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M"))))
cur.execute(sql_books)

logger.info("Import transaction")
filename_transactions = 'databases/transactions.sql'
file_transactions = open(filename_transactions, 'r')
sql_transactions = " ".join(file_transactions.readlines())
logger.info(
    "Start executing: {} at {}".format(filename_transactions, str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M"))))
cur.execute(sql_transactions)

logger.info("Finish import initial database")
connection.close()
