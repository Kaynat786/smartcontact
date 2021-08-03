console.log("this is script file");



let $yourSidebar = $(".sidebar");
$(document).on("click.toggleNav touch.toggleNav", ".sidebar", function(){ 
 $yourSidebar.toggleClass("open");
});

const searching = () =>
{
    let query= $("#search-input").value();

    console.log(query);

    if(query=="")
    {
        $(".search-result").hide();
    }

    else
    {
        console.log(query);
        $(".search-result").show();

    }
}