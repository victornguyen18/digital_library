import json
from django.shortcuts import render, redirect
from .models import Title, Author, Publisher, book
from django.contrib.auth.decorators import login_required
from django.http import HttpResponse
from django.http import JsonResponse
from django.core.serializers import serialize
from datetime import datetime
from datetime import timedelta
from django.contrib import messages


# Create your views here.
def author_create(request):
    return 0


def title_create(request):
    return 0


def get_book_info(request, barcode):
    # return HttpResponse('run')
    try:
        bookInfo = book.objects.get(barcode=barcode)
    except:
        return JsonResponse({'status': 404, 'message': 'Can not find this barcode'})
    else:
        author = ''
        if (bookInfo.title.status != 0):
            for item in bookInfo.title.author.all():
                author += item.name + ', '
            week = datetime.now() + timedelta(days=7)
            sem = datetime.now() + timedelta(days=60)
            staff = datetime.now() + timedelta(days=14)
            data = {'title': bookInfo.title.name,
                    'author': author,
                    'book_status': bookInfo.status,
                    'week_price': bookInfo.title.week_price,
                    'sem_price': bookInfo.title.sem_price,
                    'due_date_week': str(week.year) + '-' + str(week.month) + '-' + str(week.day) + ' ' +
                                     str(week.hour) + ':' + str(week.minute) + ':' + str(week.second),
                    'due_date_sem': str(sem.year) + '-' + str(sem.month) + '-' + str(sem.day) + ' ' +
                                    str(sem.hour) + ':' + str(sem.minute) + ':' + str(sem.second),
                    'due_date_staff': str(staff.year) + '-' + str(staff.month) + '-' + str(staff.day) + ' ' +
                                      str(staff.hour) + ':' + str(staff.minute) + ':' + str(staff.second)}
            return JsonResponse({'status': 200, 'data': data})
        else:
            return JsonResponse({'status': 199, 'message': 'This title is disable'})


@login_required(login_url='/log-in')
def author_index(request):
    author = Author.objects.all()
    return render(request, 'author/index.html', {
        'authors': author,
    })


def publisher_index(request):
    publishers = Publisher.objects.all()
    return render(request, 'publisher/index.html', {
        'publishers': publishers,
    })


def publisher_create(request):
    # form = AlbumForm(request.POST or None, request.FILES or None)
    # if form.is_valid():
    #     album = form.save(commit=False)
    #     album.user = request.user
    #     album.album_logo = request.FILES['album_logo']
    #     file_type = album.album_logo.url.split('.')[-1]
    #     file_type = file_type.lower()
    #     if file_type not in IMAGE_FILE_TYPES:
    #         context = {
    #             'album': album,
    #             'form': form,
    #             'error_message': 'Image file must be PNG, JPG, or JPEG',
    #         }
    #         return render(request, 'music/create_album.html', context)
    #     album.save()
    #     return render(request, 'music/detail.html', {'album': album})
    # context = {
    #     "form": form,
    # }
    return render(request, 'publisher/create.html')


def title_index(request):
    titles = Title.objects.all()
    return render(request, 'title/index.html', {
        'titles': titles,
    })
