from django import template
from django.contrib.auth.models import Group
from django.http import QueryDict
import re

register = template.Library()


@register.filter(name='has_group')
def has_group(user, group_name):
    group = Group.objects.get(name=group_name)
    return True if group in user.groups.all() else False


@register.filter
def index(List, index):
    return List[index]


@register.filter
def check_select(value_check, value_input):
    if int(value_check) == int(value_input):
        return 'selected'
    else:
        return ''
