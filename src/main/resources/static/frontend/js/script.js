let carrito = JSON.parse(localStorage.getItem("carrito")) || [];

function guardarCarrito() {
  localStorage.setItem("carrito", JSON.stringify(carrito));
}

function actualizarCarrito() {
  const lista = document.querySelector("#lista-carrito");
  lista.innerHTML = "";

  let total = 0;

  carrito.forEach((item) => {
    const li = document.createElement("li");
    li.textContent = `${item.nombre} - $${item.precio.toLocaleString()}`;
    li.classList.add("agregado"); // animación CSS
    lista.appendChild(li);
    total += item.precio;
  });

  document.querySelector("#total").textContent = total.toLocaleString();
  guardarCarrito();
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".carrito-btn").forEach((boton) => {
    boton.addEventListener("click", () => {
      const producto = boton.closest(".producto");
      const nombre = producto.querySelector("h3").textContent.trim();

      let precio = producto
        .querySelector(".precio-descuento")
        .textContent.trim()
        .replace("$", "");
      precio = parseInt(precio.split(".").join(""), 10);

      carrito.push({ nombre, precio });
      guardarCarrito();
      actualizarCarrito();

      // confirmación visual
      Swal.fire({
        icon: "success",
        title: "¡Agregado al carrito!",
        text: nombre,
        confirmButtonColor: "#00cc44",
        timer: 1500,
        showConfirmButton: false,
      });
    });
  });

  // Botón para vaciar el carrito
  const vaciarBtn = document.getElementById("vaciarCarritoBtn");
  if (vaciarBtn) {
    vaciarBtn.addEventListener("click", () => {
      Swal.fire({
        title: "¿Vaciar el carrito?",
        text: "Esta acción eliminará todos los productos",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#d33",
        cancelButtonColor: "#888",
        confirmButtonText: "Sí, vaciar",
      }).then((result) => {
        if (result.isConfirmed) {
          carrito = [];
          actualizarCarrito();

          Swal.fire({
            icon: "success",
            title: "Carrito vacío",
            showConfirmButton: false,
            timer: 1000,
          });
        }
      });
    });
  }

  actualizarCarrito();
});

