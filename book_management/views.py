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

import builder.calculate_point as cp


def homepage(request):
    cp.CalculatePointAllUser().calculate()
    return render(request, 'site/homepage.html')


def logout(request):
    dj_logout(request)
    return redirect('/login')


def login(request):
    if request.user.is_authenticated:
        return redirect('/dashboard')
    if request.method == "POST":
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(username=username, password=password)
        if user is not None:
            if User.objects.filter(pk=user.id, groups__name__in=['Part-time', 'Staff']).exists():
                dj_login(request, user)
                if not request.POST.get('remember', None):
                    # not check remember -> set session time 0
                    request.session.set_expiry(0)
                return redirect('/dashboard')
            else:
                dj_login(request, user)
                if not request.POST.get('remember', None):
                    # not check remember -> set session time 0
                    request.session.set_expiry(0)
                return redirect('/')
                # messages.error(request,
                #                "You don't have permission to access. "
                #                "Please contact your administrator to request access")
        else:
            messages.error(request, 'Wrong account or password. Please try again or click Forgot Password to reset '
                                    'password.')
        return redirect('/login')
    return render(request, 'admin/auth/login.html')


@login_required(login_url='/login')
def session_set_json(request):
    for session in json.loads(request.POST['SessionInput']):
        request.session[session['key']] = session['value']
    return JsonResponse({'status': 200})


@login_required(login_url='/login')
def dashboard(request):
    if User.objects.filter(pk=request.user.id, groups__name__in=['Part-time', 'Staff']).exists():
        detail_tran_list = Detail.objects.filter(status__in=[2, 3]) \
            .order_by('book__status', '-transaction__date', 'id')
        total_title = Title.objects.count()
        total_book = Book.objects.count()
        total_book_hiring = Book.objects.filter(status=3).count()
        total_book_overdue = Detail.objects.filter(status=3, due_date__lte=datetime.now()).count()
        return render(request, 'admin/dashboard/index.html', {
            'detail_tran_list': detail_tran_list,
            'total_book': total_book,
            'total_title': total_title,
            'total_book_hiring': total_book_hiring,
            'total_book_overdue': total_book_overdue,
        })
    else:
        messages.error(request,
                       "You don't have permission to access dashboard. "
                       "Please contact your administrator to request access")
        return redirect('/')


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
