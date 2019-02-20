from django.conf.urls import url
from . import views

app_name = 'transaction'

urlpatterns = [
    # Transaction
    url(r'^transaction$', views.transaction_index, name='transaction'),
    url(r'^transaction/create/$', views.transaction_create, name='transaction.create'),

    # Change status transaction and book
    url(r'^transaction/hired/(?P<detail_id>[0-9]+)$', views.approved_hire),
    url(r'^transaction/reject/(?P<detail_id>[0-9]+)$', views.reject),
    url(r'^transaction/return/(?P<detail_id>[0-9]+)$', views.return_book),
]
