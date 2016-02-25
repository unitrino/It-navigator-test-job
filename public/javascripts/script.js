var out_data = []

function getValues(array, search) {
  var value = [];

  array.forEach(function(values, item){
    (values.parent === search) && value.push(item);
  });

  return value;
};

function validateNumbers(event) {
    var key = window.event ? event.keyCode : event.which;

if (event.keyCode == 8 || event.keyCode == 46
 || event.keyCode == 37 || event.keyCode == 39) {
    return true;
}
else if ( key < 48 || key > 57 ) {
    return false;
}
else return true;
};


$(document).ready(function(){
    $("#changebutton").click(function(){
        $("tr[id^='row'] input[id^=comment]").removeAttr('readonly');
        $("tr[id^='row'] input[id^=number]").removeAttr('readonly');
        $("tr[id^='row'] select[id^=numb_type]").removeAttr('disabled');
    });
    $("tr[id^='row'] input[id^=comment]").change(function(){
    	console.log( "ELEM " + $(this).parents()[1].id.substr(3));
    	console.log( "ELEM " + $(this).attr("id"));
    	console.log( "VALUE " + $(this).val());
    	var parent = $(this).parents()[1].id.substr(3);
    	var key = $(this).attr("id");
    	var value = $(this).val();
    	if(getValues(out_data,parent).length === 0)
    	{
    		out_data.push({parent:parent,comments:value});
    	}
    	else
    	{
    		var ind = getValues(out_data,parent)[0];
    		out_data[ind].comments = value;
    	}
    	console.log(out_data);

    });
    $("tr[id^='row'] input[id^=number]").change(function(){
    	console.log( "ELEM " + $(this).parents()[1].id.substr(3));
    	console.log( "ELEM " + $(this).attr("id"));
    	console.log( "VALUE " + $(this).val());
    	var parent = $(this).parents()[1].id.substr(3);
    	var key = $(this).attr("id");
    	var value = $(this).val();
    	if(getValues(out_data,parent).length === 0)
    	{
    		out_data.push({parent:parent,numbers:value});
    	}
    	else
    	{
    		var ind = getValues(out_data,parent)[0]
    		out_data[ind].numbers = value
    	}
    	console.log(out_data);
    });

     $("tr[id^='row'] select[id^=numb_type]").change(function(){
            console.log( "ELEM " + $(this).parents()[1].id.substr(3));
            console.log( "ELEM " + $(this).attr("id"));
            console.log( "VALUE " + $(this).val());
            var parent = $(this).parents()[1].id.substr(3);
            var key = $(this).attr("id");
            var value = $(this).val();
            if(getValues(out_data,parent).length === 0)
            {
                out_data.push({parent:parent,numb_types:value});
            }
            else
            {
                var ind = getValues(out_data,parent)[0]
                out_data[ind].numb_types = value
            }
            console.log(out_data);
        });


    $("#savebutton").click(function(){
         $.ajax({
            url: "save_data",
            contentType: "application/json; charset=utf-8",
            type: "POST",
            cache: false,
            data: JSON.stringify(out_data),
            success: function(response){
                    $('#main_table').html(response);
            }
        });

        $("tr[id^='row'] input[id^=comment]").attr('readonly','');
        $("tr[id^='row'] input[id^=number]").attr('readonly','');
        $("tr[id^='row'] select[id^=numb_type]").attr('disabled','');
        out_data = [];





    });
}); 