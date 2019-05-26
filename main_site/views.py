# Import django lib
from django.contrib import messages
from django.contrib.auth import authenticate, login as dj_login, logout as dj_logout
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect
from django.http import JsonResponse, HttpResponse

# Import python lib
from datetime import datetime
import json

# Import Models
from django.contrib.auth.models import User
from title.models import Title, Book
from transaction.models import Detail

from recommendation.collaborative_filtering import my_recommend
import digital_library_java.python_seach.search_library as ps
import pandas as pd


def recommend(request):
    transaction = pd.DataFrame(my_recommend())
    print(transaction)
    return HttpResponse(transaction.to_html())


def search(request):
    # Post method -> save to database
    num = 12
    search_term = request.GET.get('search_term') if request.GET.get('search_term') is not None else ""
    search_option = request.GET.get('search_option') if request.GET.get('search_option') is not None else ""
    page = int(request.GET.get('page')) if request.GET.get('page') is not None else 1
    if search_option == '1':
        data_book_google = ps.get_search_book_google(search_term, num, page)
        if data_book_google['status'] == 200:
            items = json.loads(data_book_google["data"])
            total_page = -1
        else:
            items = []
            messages.error(request, "Some wrong!! Please try again")
            total_page = 0
    elif search_option == '2':
        data_book_google = ps.get_search_book_ontology(search_term, num, page)
        if data_book_google['status'] == 200:
            items = json.loads(data_book_google["data"])
            total_page = data_book_google['totalPage']
        else:
            items = []
            total_page = 0
            messages.error(request, "Some wrong!! Please try again")
    else:
        items = []
        total_page = -1
    return render(request, 'site/search results.html', {
        'search_term': search_term,
        'search_option': search_option,
        'items': items,
        'total_page': total_page
    })
