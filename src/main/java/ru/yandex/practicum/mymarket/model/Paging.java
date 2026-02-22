package ru.yandex.practicum.mymarket.model;

/**
 * Объект для хранения данных о положении страницы.
 *
 * @param pageSize    количество объектов на странице.
 * @param pageNumber  номер страницы.
 * @param hasPrevious true если есть предыдущая страница.
 * @param hasNext     true если есть следующая страница.
 */
public record Paging(int pageNumber,
                     int pageSize,
                     boolean hasPrevious,
                     boolean hasNext) {

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean getHasPrevious() {
        return hasPrevious;
    }


    public boolean getHasNext() {
        return hasNext;
    }
}
