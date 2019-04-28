from django.contrib.auth import authenticate, login as dj_login, logout as dj_logout
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect
from django.db.models import Count, Q
from django.http import HttpResponse
from django.http import JsonResponse
from django.contrib import messages

# Import Library
from datetime import datetime
import json

# Import Models
from django.contrib.auth.models import User
from title.models import Author, Publisher, Title, Book
from transaction.models import Master, Detail


def index(request):
    return render(request, 'index.html')


def logout(request):
    dj_logout(request)
    return redirect('/log-in')


def login(request):
    if request.user.is_authenticated():
        return redirect('/dashboard')
    if request.method == "POST":
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(username=username, password=password)
        if user is not None:
            if user.is_active:
                dj_login(request, user)
                return redirect('/dashboard')
            else:
                return render(request, 'user/login.html', {'error_message': 'Your account has been disabled'})
        else:
            return render(request, 'user/login.html', {
                'error_message': 'Wrong account or password. Please try again or click Forgot Password to reset '
                                 'password.'})
    return render(request, 'user/login.html')


@login_required(login_url='/log-in')
def session_set_json(request):
    for session in json.loads(request.POST['SessionInput']):
        request.session[session['key']] = session['value']
    return JsonResponse({'status': 200})


@login_required(login_url='/log-in')
def dashboard(request):
    detail_tran_list = Detail.objects.filter(status__in=[2, 3]) \
        .order_by('book__status', '-transaction__date', 'id')
    total_title = Title.objects.count()
    total_book = Book.objects.count()
    total_book_hiring = Book.objects.filter(status=3).count()
    total_book_overdue = Detail.objects.filter(status=3, due_date__lte=datetime.now()).count()
    return render(request, 'dashboard/index.html', {
        'detail_tran_list': detail_tran_list,
        'total_book': total_book,
        'total_title': total_title,
        'total_book_hiring': total_book_hiring,
        'total_book_overdue': total_book_overdue,
    })


def get_user_info(request, username):
    # return HttpResponse('run')
    try:
        user = User.objects.get(username=username)
    except User.DoesNotExist:
        return JsonResponse({'status': 404})
    else:
        data = {'first_name': user.first_name,
                'last_name': user.last_name,
                'email': user.email}
        return JsonResponse({'status': 200, 'data': data})
