from py4j.java_gateway import *
import json


def get_search_book_google(search_term, num, start):
    gateway = JavaGateway()
    google_book = gateway.entry_point.searchGoogleBook(search_term, num, start)
    google_book_obj = json.loads(google_book)
    return google_book_obj
