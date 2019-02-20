from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.contrib.auth.decorators import permission_required

from django.http import HttpResponse
# Model
from django.contrib.auth.models import User
from transaction.models import master, detail, book
from django.contrib import messages
from datetime import datetime
from django.core import serializers
import json


@permission_required('can_view_all_transactions', login_url='/log-in')
def transaction_index(request):
    transaction = detail.objects.all() \
        .order_by('-status', '-transaction__date', 'id')
    return render(request, 'transaction/index.html', {
        'transactions': transaction,
    })


@permission_required('transaction.can_create_transaction', login_url='/log-in')
def transaction_create(request):
    if (request.POST):
        barcode = request.POST.getlist('barcode[]')
        if len(barcode) > 0:
            try:
                userInput = User.objects.get(username=request.POST.get('username'))
            except User.DoesNotExist:
                messages.error(request, 'Your username does not exist')
                return render(request, 'transaction/create.html')
            else:
                barcode = request.POST.getlist('barcode[]')
                hire_type = request.POST.getlist('hire_type[]')
                price = request.POST.getlist('price[]')
                due_date = request.POST.getlist('due_date[]')
                tmp = set()
                for x in barcode:
                    try:
                        bookInfo = book.objects.get(barcode=x)
                    except book.DoesNotExist:
                        messages.error(request, 'Your barcode does not exist')
                        return redirect('transaction:transaction.create')
                    else:
                        if (bookInfo.status != 1):
                            messages.error(request, "Your book's barcode does not available")
                            return redirect('transaction:transaction.create')
                        if x in tmp:
                            messages.error(request, 'Duplicate barcode')
                            return redirect('transaction:transaction.create')
                    tmp.add(x)
                try:
                    data = master.objects.create(date=request.POST.get('transaction_date'), user=userInput)
                    for i in range(len(barcode)):
                        bookInfo = book.objects.get(barcode=barcode[i])
                        detail.objects.create(transaction_id=data.id,
                                              book=bookInfo,
                                              hire_type=hire_type[i],
                                              price=price[i],
                                              due_date=due_date[i],
                                              status=2)
                        bookInfo.status = 2
                        bookInfo.save()
                        if 'transaction' in request.session:
                            del request.session['transaction']
                    messages.success(request, 'Create new transaction success')
                    return redirect('transaction:transaction')
                except:
                    messages.error(request, 'Some input is wrong format in detail')
                else:
                    messages.success(request, 'Create new transaction success')
                return redirect('transaction:transaction.create')
        else:
            messages.error(request, 'Input some detail of transaction')
            return redirect('transaction:transaction.create')
    else:
        transaction = request.session[
            'transaction'] if 'transaction' in request.session else {'username': '', 'detail': {}}
        username = User.objects.get(username=transaction['username']) if (
                ('username' in transaction) and transaction['username'] != '') else ''
        first_name = username.first_name if username else ''
        last_name = username.last_name if username else ''
        detail_tran = transaction['detail'] if ('detail' in transaction) else {}

    return render(request, 'transaction/create.html', {
        'transaction': transaction,
        'username': username, 'first_name': first_name, 'last_name': last_name,
        'detail_tran': detail_tran})


@permission_required('transaction.can_mark_hiring', login_url='/log-in')
def approved_hire(request, detail_id):
    tran_detail = detail.objects.get(id=detail_id)
    if tran_detail.status == 2:
        # Change status in detail
        tran_detail.status = 3
        tran_detail.save()
        # Change status in book
        book_obj = book.objects.get(barcode=tran_detail.book_id)
        book_obj.status = 3
        book_obj.save()
        messages.success(request, 'Change status to hiring successful')
    else:
        messages.error(request, 'Book is not pending')
    return redirect('dashboard')


@permission_required('transaction.can_reject_hiring', login_url='/log-in')
def reject(request, detail_id):
    tran_detail = detail.objects.get(id=detail_id)
    if tran_detail.status == 2:
        # Change status in detail
        tran_detail.status = 0
        tran_detail.save()
        # Change status in book
        book_obj = book.objects.get(barcode=tran_detail.book_id)
        book_obj.status = 1
        book_obj.save()
        messages.success(request, 'Change status to rejected successful')
    else:
        messages.error(request, 'Book is not pending')
    return redirect('dashboard')


@permission_required('transaction.can_mark_returned', login_url='/log-in')
def return_book(request, detail_id):
    tran_detail = detail.objects.get(id=detail_id)
    if tran_detail.status == 3:
        # Change status in detail
        tran_detail.status = 1
        tran_detail.return_date = datetime.now()
        tran_detail.save()
        # Change status in book
        book_obj = book.objects.get(barcode=tran_detail.book_id)
        book_obj.status = 1
        book_obj.save()
        messages.success(request, 'Change status to rejected successful')
    else:
        messages.error(request, 'Book is not hiring')
    return redirect('dashboard')
