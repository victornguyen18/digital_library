# Generated by Django 2.2.1 on 2019-07-30 16:57

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('title', '0004_auto_20190719_0139'),
    ]

    operations = [
        migrations.AddField(
            model_name='title',
            name='image',
            field=models.CharField(blank=True, max_length=256, null=True),
        ),
    ]