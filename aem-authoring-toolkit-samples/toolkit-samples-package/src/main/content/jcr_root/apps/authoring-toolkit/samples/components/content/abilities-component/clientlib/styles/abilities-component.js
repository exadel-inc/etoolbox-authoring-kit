(function () {

    function elementsButtonEvent(event) {
        if (event.target.id === "warrior-abilities-button") {
            let elementsList = document.getElementById("warrior-abilities-list");
            elementsList.hidden = !elementsList.hidden;
        }
    }

    function bind() {
        document.body.addEventListener("click", elementsButtonEvent);
    }

    bind();
})();