# Generated by Django 2.2.1 on 2019-06-14 14:06

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('transaction', '0013_auto_20190614_1344'),
    ]

    operations = [
        migrations.AlterField(
            model_name='detail',
            name='book',
            field=models.ForeignKey(on_delete=django.db.models.deletion.PROTECT, to='title.Book'),
        ),
        migrations.AlterField(
            model_name='detail',
            name='transaction',
            field=models.ForeignKey(default=1, on_delete=django.db.models.deletion.PROTECT, to='transaction.Master'),
        ),
    ]
