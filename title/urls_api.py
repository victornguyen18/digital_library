from django.urls import path, re_path
from . import views

app_name = 'title_api'

urlpatterns = [
    # Title
    re_path(r'^book/api/(?P<barcode>[a-zA_Z0-9-]+)$', views.get_book_info),
    # url(r'^book/api$', views.get_book_info),
]
