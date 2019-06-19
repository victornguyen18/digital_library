# Import django lib
from django.contrib import messages
from django.contrib.auth import authenticate, login as dj_login, logout as dj_logout
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect
from django.http import JsonResponse, HttpResponse, Http404

# Import python lib
import json
import numpy as np
import resc_system.recommender_system as rs
import builder.user_similarity_calculator as rsnb
import digital_library_java.python_seach.search_library as ps

# Import Models
from django.db.models import Case, When, QuerySet
from django.contrib.auth.models import User
from title.models import Title, Publisher, Author


def get_user_based_rs(request):
    # current_user_id = 8
    current_user_id = 20
    print("Current user id: ", current_user_id)
    prediction_matrix, Y_mean = rs.recommend_cf()
    my_predictions = prediction_matrix[:, current_user_id] + Y_mean.flatten()
    pred_id_xs_sorted = np.argsort(my_predictions)
    pred_id_xs_sorted[:] = pred_id_xs_sorted[::-1]
    pred_id_xs_sorted = pred_id_xs_sorted + 1
    print(pred_id_xs_sorted)
    preserved = Case(*[When(pk=pk, then=pos) for pos, pk in enumerate(pred_id_xs_sorted)])
    book_list = Title.objects.filter(id__in=pred_id_xs_sorted, ).order_by(preserved)[:12]
    book_list = [Title.book_info_as_dict(book) for book in book_list]
    print(book_list)
    data = {'book_list': json.dumps(book_list)}
    return JsonResponse({'status': 200, 'data': data})


def get_recommendation(request):
    if not request.user.is_authenticated:
        return redirect("log-in")
    if not request.user.is_active:
        raise Http404
    current_user_id = request.user.id
    print("Current user id: ", current_user_id)
    rec_list = rsnb.RecommendationNB().get_list_recommendation(current_user_id)
    print(rec_list)
    book_id_list = []
    for i in rec_list:
        book_id_list.append(int(i[0]))
    book_list = Title.objects.filter(id__in=book_id_list)[:12]
    book_list = [Title.book_info_as_dict(book) for book in book_list]
    data = {'book_list': json.dumps(book_list)}
    return JsonResponse({'status': 200, 'data': data})


def get_popular_book(request):
    popular_book = rs.get_popular_book()
    popular_book_12 = popular_book[:12]
    print(popular_book_12)
    popular_book_list = Title.objects.all().filter(id__in=popular_book_12)
    popular_book_list = [Title.book_info_as_dict(book) for book in popular_book_list]
    data = {'popular_book_list': json.dumps(popular_book_list)}
    return JsonResponse({'status': 200, 'data': data})


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
