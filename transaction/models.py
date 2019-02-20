from django.db import models
from django.contrib.auth.models import Permission, User
from title.models import book
from datetime import datetime


# Create your models here.
class master(models.Model):
    date = models.DateTimeField()
    user = models.ForeignKey(User)


class detail(models.Model):
    book = models.ForeignKey(book)
    hire_type = models.IntegerField(default=0)
    due_date = models.DateTimeField(null=False)
    return_date = models.DateTimeField(null=True)
    price = models.FloatField(default=0)
    status = models.IntegerField(default=2)
    transaction = models.ForeignKey(master, default=1)

    class Meta:
        permissions = (("can_mark_returned", "Set book as returned"),
                       ("can_mark_hiring", "Set book as hiring"),
                       ("can_reject_hiring", "Reject as hiring"),
                       ("can_create_transaction", "Create new transaction"),
                       ("can_view_all_transactions", "View all transaction"),)

    def get_status(self):
        if self.status == 0:
            return 'Rejected'
        elif self.status == 1:
            return 'Returned'
        elif self.status == 2:
            return 'Pending'
        elif self.status == 3:
            return 'Hiring'

    def get_status_html(self):
        text = "<span class=\""
        if self.status == 0:
            text = text + "badge badge-dark"
        elif self.status == 1:
            text = text + "badge badge-primary"
        elif self.status == 2:
            text = text + "badge badge-warning"
        elif self.status == 3:
            text = text + "badge badge-success"
        text = text + "\">" + str(self.get_status()) + "</span>"
        return text

    def check_overdue(self):
        temp = str(self.due_date)
        temp = temp[0:temp.find('+')]
        datetime_object = datetime.strptime(temp, '%Y-%m-%d %H:%M:%S')
        if datetime_object < datetime.now() and self.status == 3:
            return True
        else:
            return False
