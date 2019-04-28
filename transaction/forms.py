from django import forms
from django.contrib.auth.models import User

from .models import Master


class TransactionForm(forms.ModelForm):
    class Meta:
        model = Master
        fields = ['date', 'user']
