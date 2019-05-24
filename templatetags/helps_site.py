from django import template

import json

register = template.Library()


@register.filter(name='get_image_book_google')
def get_image_book_google(value):
    return json.loads(value)['image']


@register.filter(name='get_title_book_google')
def get_title_book_google(value):
    return json.loads(value)['title']


@register.filter(name='get_author_book_google')
def get_author_book_google(value):
    return json.loads(value)['author']


@register.filter(name='get_des_book_google')
def get_des_book_google(value):
    return json.loads(value)['des']


@register.filter(name='get_year_book_google')
def get_year_book_google(value):
    return json.loads(value)['year']
