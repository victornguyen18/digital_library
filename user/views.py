# Import django lib
from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.contrib.auth.hashers import make_password

# Import python lib

# Import Models
from django.contrib.auth.models import User, Group
from user.models import Detail


@login_required(login_url='/login')
def user_index(request):
    items = User.objects.all() \
        .order_by('-id')
    return render(request, 'admin/user/index.html', {
        'items': items,
    })


def user_create(request):
    if request.POST:
        # Create user -> save to db
        print(request.POST)
        data_user = User.objects.create(
            password=make_password(request.POST.get('username')),
            is_superuser=0,
            username=request.POST.get('username').lower(),
            first_name=request.POST.get('firstname'),
            last_name=request.POST.get('lastname'),
            email=request.POST.get('email'),
            is_staff=0,
            is_active=1,
        )
        try:
            data_user.save()
            detail_user = Detail.objects.create(
                address=request.POST.get('address'),
                phone=request.POST.get('phone'),
                user_id=data_user.id
            )
            try:
                detail_user.save()
            except Exception as e:
                print("Some thing error:", e)
                messages.error(request, 'Some input is wrong format')
            else:
                messages.success(request, 'Create successfully new user')
        except Exception as e:
            print("Some thing error:", e)
            messages.error(request, 'Some input is wrong format')
        else:
            messages.success(request, 'Create successfully new user')
        return redirect('user:user')
    else:
        # Render create user form
        groups = Group.objects.all().order_by('id')
        return render(request, 'admin/user/create.html', {
            'groups': groups,
        })
