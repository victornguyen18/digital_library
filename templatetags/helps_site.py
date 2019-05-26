from django import template

import json

register = template.Library()


@register.filter(name='get_image_book_google')
def get_image_book_google(value):
    if type(value) == dict:
        tmp = value
    else:
        tmp = json.loads(value)
    if "image" in tmp:
        return tmp["image"]
    else:
        return "/static/images/logo-IU.jpg"


@register.filter(name='get_title_book_google')
def get_title_book_google(value):
    if type(value) == dict:
        return value["title"]
    else:
        return json.loads(value)['title']


@register.filter(name='get_author_book_google')
def get_author_book_google(value):
    if type(value) == dict:
        return value["author"]
    else:
        return json.loads(value)['author']


@register.filter(name='get_des_book_google')
def get_des_book_google(value):
    if type(value) == dict:
        tmp = value
    else:
        tmp = json.loads(value)
    if "des" in tmp:
        return tmp["des"]
    else:
        return ""


@register.filter(name='get_year_book_google')
def get_year_book_google(value):
    if type(value) == dict:
        return value["year"]
    else:
        return json.loads(value)['year']
