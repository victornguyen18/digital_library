from py4j.java_gateway import *
import json


def get_search_book_google(search_term, num, page):
    try:
        gateway = JavaGateway()
        google_book = gateway.entry_point.searchGoogleBook(search_term, num, page)
        google_book_obj = json.loads(google_book)
        return google_book_obj
    except:
        google_book_obj = dict()
        google_book_obj["status"] = 404
        google_book_obj["message"] = "Something wrong!! Please try again"
        google_book_obj["data"] = "[]"
        return google_book_obj


def get_search_book_ontology(search_term, num, page):
    try:
        gateway = JavaGateway()
        books = gateway.entry_point.searchOntology(search_term, num, page)
        books_obj = json.loads(books)
        return books_obj
    except:
        books_obj = dict()
        books_obj["status"] = 404
        books_obj["message"] = "Something wrong!! Please try again"
        books_obj["data"] = {}
        return books_obj
