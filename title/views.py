# Import django lib
import os

from django.conf import settings
from django.contrib import messages
from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required, permission_required
from django.http import JsonResponse
from django.core.files.storage import default_storage
# Import python lib
from datetime import datetime, timedelta

# Import Models
from django.utils.datastructures import MultiValueDictKeyError

from .models import Title, Author, Publisher, Book


@login_required(login_url='/login')
def author_index(request):
    author = Author.objects.all()
    return render(request, 'admin/author/index.html', {
        'authors': author,
    })


@login_required(login_url='/login')
def author_create(request):
    return 0


def publisher_index(request):
    publishers = Publisher.objects.all()
    return render(request, 'admin/publisher/index.html', {
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
    return render(request, 'admin/publisher/create.html')


@permission_required('title.view_title', login_url='/admin/publisher')
def title_index(request):
    titles = Title.objects.all()
    return render(request, 'admin/title/index.html', {
        'titles': titles,
    })


@login_required(login_url='/login')
def title_create(request):
    if request.POST:
        title = Title.objects.create()
        title.name = request.POST.get('name')
        title.faculty = request.POST.get('faculty')
        title.location = request.POST.get('location')
        title.isbn = request.POST.get('isbn')
        title.week_price = request.POST.get('week_price')
        title.sem_price = request.POST.get('sem_price')
        try:
            image_upload = request.FILES['image']
            has_image = True
        except MultiValueDictKeyError:
            has_image = False
        if has_image:
            name_file = 'book_image_' + str(datetime.now()) + '.jpg'
            save_path = os.path.join(settings.MEDIA_ROOT, name_file)
            path_db = "/static/images/" + name_file
            default_storage.save(save_path, image_upload)
            title.image = path_db
        title.save()
        messages.success(request, 'Edit title successful')
        return redirect('title:title.index')
    return render(request, 'admin/title/create.html', {})


def title_edit(request, title_id):
    title = Title.objects.get(id=title_id)
    print(request.FILES)
    if request.POST:
        title.name = request.POST.get('name')
        title.faculty = request.POST.get('faculty')
        title.location = request.POST.get('location')
        title.isbn = request.POST.get('isbn')
        title.week_price = request.POST.get('week_price')
        title.sem_price = request.POST.get('sem_price')
        try:
            image_upload = request.FILES['image']
            has_image = True
        except MultiValueDictKeyError:
            has_image = False
        if has_image:
            name_file = 'book_image_' + str(datetime.now()) + '.jpg'
            save_path = os.path.join(settings.MEDIA_ROOT, name_file)
            path_db = "/static/images/" + name_file
            default_storage.save(save_path, image_upload)
            title.image = path_db
        title.save()
        messages.success(request, 'Edit title successful')
        return redirect('title:title.index')
    return render(request, 'admin/title/edit.html', {'title': title})


def get_book_info(request, barcode):
    """
    Get information of book by ajax
    :param request:
    :param barcode:
    :return: JsonResponse
    """
    try:
        book_info = Book.objects.get(barcode=barcode)
    except Book.DoesNotExist:
        return JsonResponse({'status': 404, 'message': 'Can not find this barcode'})
    else:
        author = ''
        if book_info.title.status != 0:
            for item in book_info.title.author.all():
                author += item.name + ', '
            week = datetime.now() + timedelta(days=7)
            sem = datetime.now() + timedelta(days=60)
            staff = datetime.now() + timedelta(days=14)
            data = {'title': book_info.title.name,
                    'author': author,
                    'book_status': book_info.status,
                    'week_price': book_info.title.week_price,
                    'sem_price': book_info.title.sem_price,
                    'due_date_week': str(week.year) + '-' + str(week.month) + '-' + str(week.day) + ' ' + str(
                        week.hour) + ':' + str(week.minute) + ':' + str(week.second),
                    'due_date_sem': str(sem.year) + '-' + str(sem.month) + '-' + str(sem.day) + ' ' + str(
                        sem.hour) + ':' + str(sem.minute) + ':' + str(sem.second),
                    'due_date_staff': str(staff.year) + '-' + str(staff.month) + '-' + str(staff.day) + ' ' + str(
                        staff.hour) + ':' + str(staff.minute) + ':' + str(staff.second)}
            return JsonResponse({'status': 200, 'data': data})
        else:
            return JsonResponse({'status': 199, 'message': 'This title is disable'})
