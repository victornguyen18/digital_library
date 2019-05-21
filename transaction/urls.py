from django.urls import path, re_path
from . import views

app_name = 'transaction'

urlpatterns = [
    # Transaction
    path('transaction', views.transaction_index, name='transaction'),
    path('transaction/create', views.transaction_create, name='transaction.create'),

    # Change status transaction and book
    path('transaction/hired/<int:detail_id>', views.approved_hire),
    path('transaction/reject/<int:detail_id>', views.reject),
    path('transaction/return/<int:detail_id>', views.return_book),
    #
    # url(r'^transaction/hired/(?P<detail_id>[0-9]+)$', views.approved_hire),
    # url(r'^transaction/reject/(?P<detail_id>[0-9]+)$', views.reject),
    # url(r'^transaction/return/(?P<detail_id>[0-9]+)$', views.return_book),
]
