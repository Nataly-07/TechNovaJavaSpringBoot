// Manejo de favoritos
document.addEventListener('DOMContentLoaded', function() {
    if (!usuarioId) {
        return; // Si no hay usuario, salir
    }
    
    const favoritoBtns = document.querySelectorAll('.favorito-btn');
    
    // Verificar estado inicial de favoritos
    async function verificarFavoritos() {
        try {
            const response = await fetch(`/api/favoritos/usuario/${usuarioId}`);
            if (!response.ok) {
                return;
            }
            
            const favoritos = await response.json();
            const favoritoIds = favoritos.map(f => f.productoId);
            
            favoritoBtns.forEach(btn => {
                const productoId = parseInt(btn.getAttribute('data-producto-id'));
                if (favoritoIds.includes(productoId)) {
                    btn.style.background = '#e63946';
                    btn.textContent = '❤️';
                } else {
                    btn.style.background = '#ff6b9d';
                    btn.textContent = '🤍';
                }
            });
        } catch (error) {
            console.error('Error al verificar favoritos:', error);
        }
    }
    
    // Agregar/quitar favorito
    favoritoBtns.forEach(btn => {
        btn.addEventListener('click', async function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const productoId = parseInt(this.getAttribute('data-producto-id'));
            await toggleFavorito(productoId, this);
        });
    });
    
    async function toggleFavorito(productoId, btn) {
        try {
            const response = await fetch(`/api/favoritos/usuario/${usuarioId}/producto/${productoId}/toggle`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error('Error al actualizar favorito');
            }
            
            const esFavorito = await response.json();
            
            // Actualizar el botón visualmente
            if (esFavorito) {
                btn.style.background = '#e63946';
                btn.textContent = '❤️';
                if (typeof Swal !== 'undefined') {
                    Swal.fire({
                        icon: 'success',
                        title: 'Agregado a favoritos',
                        timer: 1500,
                        showConfirmButton: false
                    });
                }
            } else {
                btn.style.background = '#ff6b9d';
                btn.textContent = '🤍';
                if (typeof Swal !== 'undefined') {
                    Swal.fire({
                        icon: 'info',
                        title: 'Eliminado de favoritos',
                        timer: 1500,
                        showConfirmButton: false
                    });
                }
            }
            
            // Recargar la página para actualizar el contador
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } catch (error) {
            console.error('Error al actualizar favorito:', error);
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudo actualizar el favorito'
                });
            }
        }
    }
    
    // Verificar favoritos al cargar
    verificarFavoritos();
});

