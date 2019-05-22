# Import django lib
from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from django.http import JsonResponse

# Import python lib
from datetime import datetime, timedelta

# Import Models
from .models import Title, Author, Publisher, Book


# Create your views here.
def author_create(request):
    return 0


def title_create(request):
    return 0


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


@login_required(login_url='/log-in')
def author_index(request):
    author = Author.objects.all()
    return render(request, 'admin/author/index.html', {
        'authors': author,
    })


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


def title_index(request):
    titles = Title.objects.all()
    return render(request, 'admin/title/index.html', {
        'titles': titles,
    })
