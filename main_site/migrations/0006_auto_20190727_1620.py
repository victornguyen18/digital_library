# Generated by Django 2.2.1 on 2019-07-27 16:20

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('main_site', '0005_auto_20190725_1223'),
    ]

    operations = [
        migrations.CreateModel(
            name='UserSimilarity',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('created', models.DateField()),
                ('source', models.IntegerField(db_index=True)),
                ('target', models.IntegerField()),
                ('similarity', models.DecimalField(decimal_places=7, max_digits=8)),
            ],
            options={
                'db_table': 'user_similarity',
            },
        ),
        migrations.RenameModel(
            old_name='Similarity',
            new_name='BookSimilarity',
        ),
        migrations.AlterModelTable(
            name='booksimilarity',
            table='book_similarity',
        ),
    ]
