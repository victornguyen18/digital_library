from django import template
from django.contrib.auth.models import Group

register = template.Library()


@register.filter(name='has_group')
def has_group(user, group_name):
    group = Group.objects.get(name=group_name)
    return True if group in user.groups.all() else False


@register.filter(name='get_pagination')
# def get_pagination(total_page=-1):
def get_pagination(request, total_page):
    if total_page == -1:
        total_page = 30
    search_term = request.GET.get('search_term') if request.GET.get('search_term') is not None else ""
    search_option = request.GET.get('search_option') if request.GET.get('search_option') is not None else ""
    url_request = request.path + "?search_option=" + search_option + "&search_term=" + search_term.replace(' ', '+')
    current_page = int(request.GET.get('page')) if request.GET.get('page') is not None else 1
    html = ""
    if total_page > 1:
        html += '<div class="pagination">'
        html += '<ul class="d-flex flex-row align-items-start justify-content-start">'

        # Having previous page
        if current_page - 1 > 1:
            link = url_request + "&page=" + str(current_page - 1)
            html += '<li><a href="' + link + '">Previous</a></li>'

        begin = current_page - 5 if (current_page - 5 > 1) else 1
        end = current_page + 5 if (current_page + 5 < total_page or total_page == -1) else total_page
        for i in range(begin, end + 1):
            link = url_request + "&page=" + str(i)
            if current_page == i:
                html += '<li class="active"><a href="' + link + '">' + str(i) + '.</a></li>'
            else:
                html += '<li><a href="' + link + '">' + str(i) + '.</a></li>'

        # Having next page
        if current_page + 1 < total_page or total_page == -1:
            link = url_request + "&page=" + str(current_page + 1)
            html += '<li><a href="' + link + '">Next</a></li>'

        #    + '<li class="active"><a href="#">1.</a></li>'
        # +'<li><a href="">2.</a></li>'
        # +'<li><a href="#">3.</a></li>'
        # +'<li><a href="#">4.</a></li>'
        # +'<li><a href="#">5.</a></li>'
        html += '</ul>'
        html += "</div>"
        # # return "Thang"
    return html
# $html = "";
#         if (count($requests) > 0) {
#             $temp = array();
#             foreach ($requests as $key => $request) {
#                 array_push($temp, $key . '=' . $request);
#             }
#             $url = $link . '?' . implode('&', $temp) . '&page=';
#         } else {
#             $url = $link . "?page=";
#         }
#
#         if ($totalPages > 1) {
#
#             $html = '<nav aria-label=""><ul class="pnc-pagin pagination no-margin ">';
#             $html .= "<li class='page-item'><a href='" . $url . "1' class='page-link first' ><i class='fa fa-fast-backward'></i></a></li>";
#             // 1.
#             if ($currentPage - 1 >= 1) {
#
#                 $link = $url . ($currentPage - 1);
#                 $html .= "<li class='page-item'><a href='" . $link . "' class='prev page-link' ><i class='fa fa-angle-left'></i></a></li>";
#             }
#
#             // 2.
#             $i = (($currentPage - 4) > 1) ? ($currentPage - 4) : 1;
#             $end = (($currentPage + 4) < $totalPages) ? ($currentPage + 4) : $totalPages;
#             for (; $i <= $end; $i++) {
#                 $link = $url . $i;
#                 if ($i == $currentPage) {
#                     $html .= "<li class='page-item active'><a class='page-link' href='" . $link . "' >" . $i . "</a></li>";
#                 } else {
#                     $html .= "<li class='page-item'><a class='page-link' href='" . $link . "'>" . $i . "</a></li>";
#                 }
#             }
#
#             // 3.
#             if ($totalPages >= $currentPage + 1) {
#                 $link = $url . ($currentPage + 1);
#                 $html .= "<li class='page-item'><a href='" . $link . "' class='next page-link' ><i class='fa fa-angle-right'></i></a></li>";
#             }
#             $html .= "<li class='page-item'><a href='" . $url . $totalPages . "' class='first page-link' ><i class='fa fa-fast-forward'></i></a></li>";
#             $html .= '</ul></nav>';
#         }
#         return $html;
