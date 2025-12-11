/**
 * ANIMACIONES DE LOGIN/LOGOUT - OASIS DIGITAL
 *
 * Este script maneja las animaciones de bienvenida y despedida.
 * Detecta los parámetros ?loginSuccess=true y ?logout=true en la URL
 * y muestra las animaciones correspondientes.
 */

document.addEventListener("DOMContentLoaded", function () {
  // Detectar parámetros en la URL
  const urlParams = new URLSearchParams(window.location.search);
  const loginSuccess = urlParams.get("loginSuccess");
  const logout = urlParams.get("logout");

  // ===== ANIMACIÓN DE LOGIN EXITOSO =====
  if (loginSuccess === "true") {
    showLoginSuccessAnimation();
    // Limpiar parámetro de la URL después de mostrar
    setTimeout(() => {
      const newUrl = window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
    }, 3000);
  }

  // ===== ANIMACIÓN DE LOGOUT =====
  if (logout === "true") {
    showLogoutAnimation();
    // Limpiar parámetro de la URL después de mostrar
    setTimeout(() => {
      const newUrl = window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
    }, 3000);
  }
});

/**
 * Muestra animación de login exitoso (fondo verde con corona)
 */
/**
 * Muestra animación de login exitoso (fondo verde con corona)
 */
function showLoginSuccessAnimation() {
  // Crear overlay
  const overlay = document.createElement("div");
  overlay.className = "login-success-overlay";
  overlay.style.cssText =
    "position: fixed !important; top: 0 !important; left: 0 !important; width: 100% !important; height: 100% !important; background: #009B77 !important; display: flex !important; align-items: center !important; justify-content: center !important; z-index: 2147483647 !important;";
  overlay.innerHTML = `
        <div style="text-align: center !important; position: relative !important; z-index: 2147483647 !important; font-family: 'Outfit', sans-serif !important;">
            <div style="font-size: 100px !important; margin-bottom: 30px !important; line-height: 1 !important;">👑</div>
            <h2 style="color: #FFFFFF !important; font-size: 56px !important; font-weight: 900 !important; margin: 0 0 15px 0 !important; text-shadow: 0 4px 8px rgba(0,0,0,0.3) !important; letter-spacing: 1px !important;">¡Bienvenido!</h2>
            <p style="color: #FFFFFF !important; font-size: 24px !important; font-weight: 600 !important; margin: 0 !important; text-shadow: 0 2px 4px rgba(0,0,0,0.3) !important;">Inicio de sesión exitoso</p>
        </div>
    `;

  document.body.appendChild(overlay);

  // Animar salida después de 2.5 segundos
  setTimeout(() => {
    overlay.style.opacity = "0";
    overlay.style.transition = "opacity 0.5s ease-out";
    setTimeout(() => {
      overlay.remove();
    }, 500);
  }, 2500);
}

/**
 * Muestra animación de logout (fondo rojo oscurecido con corona)
 */
function showLogoutAnimation() {
  // Crear overlay
  const overlay = document.createElement("div");
  overlay.className = "logout-overlay";
  overlay.style.cssText =
    "position: fixed !important; top: 0 !important; left: 0 !important; width: 100% !important; height: 100% !important; background: #8B0000 !important; display: flex !important; align-items: center !important; justify-content: center !important; z-index: 2147483647 !important;";
  overlay.innerHTML = `
        <div style="text-align: center !important; position: relative !important; z-index: 2147483647 !important; font-family: 'Outfit', sans-serif !important;">
            <div style="font-size: 100px !important; margin-bottom: 30px !important; line-height: 1 !important;">👑</div>
            <h2 style="color: #FFFFFF !important; font-size: 56px !important; font-weight: 900 !important; margin: 0 0 15px 0 !important; text-shadow: 0 4px 8px rgba(0,0,0,0.3) !important; letter-spacing: 1px !important;">¡Hasta pronto!</h2>
            <p style="color: #FFFFFF !important; font-size: 24px !important; font-weight: 600 !important; margin: 0 !important; text-shadow: 0 2px 4px rgba(0,0,0,0.3) !important;">Sesión cerrada correctamente</p>
        </div>
    `;

  document.body.appendChild(overlay);

  // MODIFICADO PARA ANGULAR SPA:
  // Animar salida después de 2.0 segundos para permitir ver el Home tras la redirección SPA
  setTimeout(() => {
    overlay.style.opacity = "0";
    overlay.style.transition = "opacity 0.5s ease-out";
    setTimeout(() => {
      overlay.remove();
    }, 500);
  }, 2000);
}

/**
 * Función auxiliar para mostrar notificaciones de éxito/error
 * (puede usarse en formularios)
 */
function showNotification(message, type = "success") {
  const notification = document.createElement("div");
  notification.className = `notification notification-${type}`;
  notification.textContent = message;

  const style = document.createElement("style");
  style.textContent = `
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 16px 24px;
            border-radius: 8px;
            color: white;
            font-weight: 600;
            z-index: 10000;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: slideInRight 0.3s ease-out, slideOutRight 0.3s ease-in 2.7s forwards;
        }
        
        .notification-success {
            background: linear-gradient(135deg, #009B77, #00c896);
        }
        
        .notification-error {
            background: linear-gradient(135deg, #D22B2B, #ff4545);
        }
        
        @keyframes slideInRight {
            from {
                transform: translateX(400px);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        
        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(400px);
                opacity: 0;
            }
        }
    `;

  document.head.appendChild(style);
  document.body.appendChild(notification);

  setTimeout(() => {
    notification.remove();
    style.remove();
  }, 3000);
}

// Exportar funciones para uso global
window.showLoginSuccessAnimation = showLoginSuccessAnimation;
window.showLogoutAnimation = showLogoutAnimation;
window.showNotification = showNotification;
