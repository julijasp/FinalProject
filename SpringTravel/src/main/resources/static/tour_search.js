const continentSelect = document.querySelector('#continent');
continentSelect.addEventListener('change', (event) => {
    var url = new URLSearchParams(window.location.search);
    url.set('continent', event.target.value);
    window.location.search = url;
});

const countrySelect = document.querySelector('#country');
countrySelect.addEventListener('change', (event) => {
    var url = new URLSearchParams(window.location.search);
    url.set('country', event.target.value);
    window.location.search = url;
});


function filterList(){
var select = document.getElementById("country2");
    var text = select.options[select.selectedIndex].text;
    var table = document.getElementById("myTable");
  var tr = table.getElementsByTagName("tr");
    for (i = 0; i < tr.length; i++) {
      td = tr[i].getElementsByTagName("td")[1]; //[1] = kolonnas numurs kur ir valsts (sākot no 0.)
      if (td) {
        txtValue = td.textContent || td.innerText;
        if (txtValue.indexOf(text) > -1 || select.selectedIndex == 0) {
          tr[i].style.display = "";
        } else {
          tr[i].style.display = "none";
        }
      }

    }
}