from py4j.java_gateway import *
import json


def get_search_book_google(search_term, page):
    gateway = JavaGateway()
    google_book = gateway.entry_point.searchGoogleBook(search_term, page)
    google_book_obj = json.loads(google_book)
    return google_book_obj
