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
    search_term = request.GET.get('search_term')
    search_option = request.GET.get('search_option')
    if search_option == '1':
        num = 12
        page = int(request.GET.get('page')) - 1 if request.GET.get('page') is not None else 0
        start = num * page
        data_book_google = ps.get_search_book_google(search_term, num, start)
        if data_book_google['status'] == 200:
            items = json.loads(data_book_google["data"])
        else:
            items = []
            messages.error(request, "Some wrong!! Please try again")
            print(items["message"])
    else:
        items = []
    return render(request, 'site/search results.html', {
        'search_term': search_term, 'items': items
    })
