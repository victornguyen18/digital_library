from django import template
from django.contrib.auth.models import Group

import json

register = template.Library()


@register.filter(name='has_group')
def has_group(user, group_name):
    group = Group.objects.get(name=group_name)
    return True if group in user.groups.all() else False


@register.filter(name='get_pagination')
# def get_pagination(total_page=-1):
def get_pagination(request, total_page):
    if total_page == -1:
        total_page = 30
    search_term = request.GET.get('search_term') if request.GET.get('search_term') is not None else ""
    search_option = request.GET.get('search_option') if request.GET.get('search_option') is not None else ""
    url_request = request.path + "?search_option=" + search_option + "&search_term=" + search_term.replace(' ', '+')
    current_page = int(request.GET.get('page')) if request.GET.get('page') is not None else 1
    html = ""
    if total_page > 1:
        html += '<div class="pagination">'
        html += '<ul class="d-flex flex-row align-items-start justify-content-start">'

        # Having previous page
        if current_page - 1 > 1:
            link = url_request + "&page=" + str(current_page - 1)
            html += '<li><a href="' + link + '">Previous</a></li>'

        begin = current_page - 5 if (current_page - 5 > 1) else 1
        end = current_page + 5 if (current_page + 5 < total_page or total_page == -1) else total_page
        for i in range(begin, end + 1):
            link = url_request + "&page=" + str(i)
            if current_page == i:
                html += '<li class="active"><a href="' + link + '">' + str(i) + '.</a></li>'
            else:
                html += '<li><a href="' + link + '">' + str(i) + '.</a></li>'

        # Having next page
        if current_page + 1 < total_page or total_page == -1:
            link = url_request + "&page=" + str(current_page + 1)
            html += '<li><a href="' + link + '">Next</a></li>'

        #    + '<li class="active"><a href="#">1.</a></li>'
        # +'<li><a href="">2.</a></li>'
        # +'<li><a href="#">3.</a></li>'
        # +'<li><a href="#">4.</a></li>'
        # +'<li><a href="#">5.</a></li>'
        html += '</ul>'
        html += "</div>"
        # # return "Thang"
    return html


@register.filter(name='index')
def index(item_list, position):
    return item_list[position]


@register.filter(name='check_select')
def check_select(value_check, value_input):
    if int(value_check) == int(value_input):
        return 'selected'
    else:
        return ''


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
