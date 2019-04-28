# Import django
from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.contrib.auth.hashers import make_password
from django.http import HttpResponse

# Import Models
from django.contrib.auth.models import User, Group
from user.models import Detail

# Import python lib
from datetime import datetime


@login_required(login_url='/log-in')
def user_index(request):
    items = User.objects.all() \
        .order_by('-id')
    return render(request, 'user/index.html', {
        'items': items,
    })


def user_create(request):
    if request.POST:
        data_user = User.objects.create(
            password=make_password(request.POST.get('username')),
            is_superuser=0,
            username=request.POST.get('username'),
            first_name=request.POST.get('first_name'),
            last_name=request.POST.get('last_name'),
            email=request.POST.get('email'),
            is_staff=0,
            is_active=1,
        )
        try:
            data_user.save()
            detail_user = Detail.object.create(
                address=request.POST.get('address'),
                phone=request.POST.get('phone'),
                user_id=data_user.id
            )
            try:
                detail_user.save()
            except:
                messages.error(request, 'Some input is wrong format')
            else:
                messages.success(request, 'Create successfully new user')
        except:
            messages.error(request, 'Some input is wrong format')
        else:
            messages.success(request, 'Create successfully new user')
        return redirect('user:user')
    else:
        groups = Group.objects.all().order_by('id')
        return render(request, 'user/create.html', {
            'groups': groups,
        })
