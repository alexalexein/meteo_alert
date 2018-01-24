package agarcia.padir;

/**
 * Created by agarcia on 09/01/2018.
 */

interface OnAddOrEditRequested {

    void addFragmentRequested(String request, String[] parameters);
    void mainFragmentRequested(String request, String[] parameters);
}
