from django.conf.urls import url
from . import views

app_name = 'title_api'

urlpatterns = [
    # Title
    url(r'^book/api/(?P<barcode>[a-zA_Z0-9-]+)$', views.get_book_info),
    # url(r'^book/api$', views.get_book_info),
]
