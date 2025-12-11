// Efectos para el sistema Oasis Digital - Green Theme Edition
document.addEventListener("DOMContentLoaded", function () {
  // Efecto de splash screen "Letra por Letra" (Green Theme)
  function showSplashScreen() {
    const splashScreen = document.createElement("div");
    splashScreen.id = "oasis-splash-green";
    splashScreen.innerHTML = `
            <div class="text-center">
                <div class="mb-6">
                    <i class="fas fa-hotel fa-3x text-primary animate-pulse" style="color: #10b981; font-size: 3rem;"></i>
                </div>
                <h1 class="text-5xl font-bold tracking-widest text-white mb-4" style="font-family: 'Inter', sans-serif; font-size: 3rem; font-weight: 700; letter-spacing: 0.1em; color: white;">
                    <span class="splash-letter" style="animation-delay: 0.1s">O</span>
                    <span class="splash-letter" style="animation-delay: 0.2s">A</span>
                    <span class="splash-letter" style="animation-delay: 0.3s">S</span>
                    <span class="splash-letter" style="animation-delay: 0.4s">I</span>
                    <span class="splash-letter" style="animation-delay: 0.5s">S</span>
                    <span class="text-primary splash-letter" style="animation-delay: 0.6s; color: #10b981;">&nbsp;</span>
                    <span class="text-primary splash-letter" style="animation-delay: 0.7s; color: #10b981;">D</span>
                    <span class="text-primary splash-letter" style="animation-delay: 0.8s; color: #10b981;">I</span>
                    <span class="text-primary splash-letter" style="animation-delay: 0.9s; color: #10b981;">G</span>
                    <span class="text-primary splash-letter" style="animation-delay: 1.0s; color: #10b981;">I</span>
                    <span class="text-primary splash-letter" style="animation-delay: 1.1s; color: #10b981;">T</span>
                    <span class="text-primary splash-letter" style="animation-delay: 1.2s; color: #10b981;">A</span>
                    <span class="text-primary splash-letter" style="animation-delay: 1.3s; color: #10b981;">L</span>
                </h1>
                <div class="splash-line" style="height: 2px; background: #10b981; margin: 1rem auto; width: 0; animation: lineExpand 2.5s ease-in-out forwards; animation-delay: 1s;"></div>
                <p class="text-secondary-400 text-sm uppercase tracking-widest mt-4 fade-in" style="animation-delay: 1.5s; color: #94a3b8; font-size: 0.875rem; text-transform: uppercase; letter-spacing: 0.1em; opacity: 0; animation: fadeIn 1s ease-out forwards 1.5s;">Experiencia de Lujo</p>
            </div>
        `;

    splashScreen.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: #0f172a; /* Dark Slate Background */
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 999999;
            animation: splashFadeOut 1s ease-in-out forwards;
            animation-delay: 3.5s;
        `;

    // Ocultar el contenido del body mientras se muestra el splash
    document.body.style.overflow = "hidden";
    document.body.appendChild(splashScreen);

    setTimeout(() => {
      splashScreen.remove();
      document.body.style.overflow = "auto";
    }, 4500);
  }

  // Agregar estilos CSS para animaciones (Green Theme)
  const style = document.createElement("style");
  style.textContent = `
        @keyframes splashFadeOut {
            0% { opacity: 1; }
            100% { opacity: 0; visibility: hidden; }
        }

        @keyframes letterFadeIn {
            0% { opacity: 0; transform: translateY(20px); filter: blur(10px); }
            100% { opacity: 1; transform: translateY(0); filter: blur(0); }
        }

        @keyframes lineExpand {
            0% { width: 0; opacity: 0; }
            50% { width: 200px; opacity: 1; }
            100% { width: 200px; opacity: 0; }
        }
        
        @keyframes fadeIn {
            to { opacity: 1; }
        }

        .splash-letter {
            display: inline-block;
            opacity: 0;
            animation: letterFadeIn 1s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
        }

        .animate-pulse {
            animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
        }

        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: .5; }
        }
    `;
  document.head.appendChild(style);

  // Función para mostrar efecto de bienvenida personalizado
  function showWelcomeEffect(userName, userRole) {
    showOperationEffect("bienvenida", `¡Bienvenido ${userName}!`);
  }

  // Función para mostrar efecto de despedida
  function showGoodbyeEffect() {
    showOperationEffect("despedida", "¡Hasta pronto!");
  }

  // Detectar si el usuario acaba de hacer login exitoso
  const urlParams = new URLSearchParams(window.location.search);
  const isLoginSuccessParam = urlParams.get("loginSuccess") === "true";
  const isDashboard = window.location.pathname.includes("dashboard");
  const isHomePage =
    window.location.pathname === "/" ||
    window.location.pathname === "/index.html";
  const isFirstVisit = !sessionStorage.getItem("splashShown");

  // Mostrar splash si:
  // 1. Es login exitoso
  // 2. Es la primera vez que entra al dashboard
  // 3. Es la home page y no se ha mostrado antes en esta sesión
  const shouldShowSplash =
    isLoginSuccessParam ||
    (isDashboard && !sessionStorage.getItem("dashboardVisited")) ||
    (isHomePage && isFirstVisit);

  // Función para mostrar el NUEVO Welcome Screen (Green Crown)
  function showNewWelcomeScreen(userName, userRole) {
    const welcomeScreen = document.createElement("div");
    welcomeScreen.id = "oasis-welcome-green";

    // Determinar el texto del rol para mostrar
    let roleText = "Usuario";
    if (userRole.includes("ADMIN")) roleText = "Administrador";
    else if (userRole.includes("CLIENTE")) roleText = "Cliente";
    else if (userRole.includes("EMPLEADO")) roleText = "Empleado";
    else roleText = userRole || "Usuario";

    welcomeScreen.innerHTML = `
            <div style="text-align: center; color: white; animation: fadeInUp 0.8s ease-out;">
                <!-- Crown Icon -->
                <div style="font-size: 5rem; color: #fbbf24; margin-bottom: 1.5rem; filter: drop-shadow(0 4px 6px rgba(0,0,0,0.2)); animation: floatCrown 3s ease-in-out infinite;">
                    <i class="fas fa-crown"></i>
                </div>

                <!-- Title -->
                <h1 style="font-size: 3.5rem; font-weight: 700; margin-bottom: 0.5rem; font-family: 'Inter', sans-serif;">¡Bienvenido de vuelta!</h1>

                <!-- User Name -->
                <h2 style="font-size: 2rem; font-weight: 400; margin-bottom: 1.5rem; opacity: 0.9;">${userName}</h2>

                <!-- Role Badge -->
                <div style="display: inline-block; background: rgba(255,255,255,0.15); padding: 0.75rem 2rem; border-radius: 9999px; font-weight: 600; font-size: 1.1rem; margin-bottom: 4rem; backdrop-filter: blur(5px); border: 1px solid rgba(255,255,255,0.2);">
                    ${roleText}
                </div>

                <!-- Loading Section -->
                <div style="opacity: 0.8;">
                    <p style="margin-bottom: 1rem; font-size: 1rem; letter-spacing: 0.05em;">Preparando tu espacio de trabajo...</p>
                    <div class="loading-dots" style="display: flex; justify-content: center; gap: 0.5rem;">
                        <span style="width: 12px; height: 12px; background: white; border-radius: 50%; animation: bounceDot 1.4s infinite ease-in-out both;"></span>
                        <span style="width: 12px; height: 12px; background: white; border-radius: 50%; animation: bounceDot 1.4s infinite ease-in-out both; animation-delay: -0.32s;"></span>
                        <span style="width: 12px; height: 12px; background: white; border-radius: 50%; animation: bounceDot 1.4s infinite ease-in-out both; animation-delay: -0.16s;"></span>
                    </div>
                </div>
            </div>
            
            <!-- Background Decorations -->
            <i class="fas fa-star" style="position: absolute; top: 10%; right: 10%; font-size: 3rem; color: rgba(255,255,255,0.1); transform: rotate(15deg);"></i>
            <i class="fas fa-book" style="position: absolute; bottom: 15%; left: 10%; font-size: 4rem; color: rgba(255,255,255,0.05); transform: rotate(-10deg);"></i>
        `;

    welcomeScreen.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, #10b981 0%, #059669 100%); /* Green Gradient */
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 999999;
            animation: splashFadeOut 1s ease-in-out forwards;
            animation-delay: 3.5s;
        `;

    document.body.style.overflow = "hidden";
    document.body.appendChild(welcomeScreen);

    setTimeout(() => {
      welcomeScreen.remove();
      document.body.style.overflow = "auto";
    }, 4500);
  }

  // Agregar estilos extra para el nuevo welcome screen
  const extraStyle = document.createElement("style");
  extraStyle.textContent = `
        @keyframes floatCrown {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-10px); }
        }
        @keyframes bounceDot {
            0%, 80%, 100% { transform: scale(0); }
            40% { transform: scale(1); }
        }
        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
  document.head.appendChild(extraStyle);

  // CASO 1: Login Exitoso -> Mostrar NUEVO Welcome Screen (Corona Verde)
  /*
  if (
    isLoginSuccessParam ||
    (isDashboard && !sessionStorage.getItem("dashboardVisited"))
  ) {
    if (!sessionStorage.getItem("welcomeShown")) {
      // Leer datos del usuario desde los atributos data del body
      const bodyElement = document.body;
      let userName = bodyElement.getAttribute("data-user-name") || "Usuario";
      let userRole = bodyElement.getAttribute("data-user-role") || "ROLE_USER";

      // Si no se encontraron en el body, intentar leer del DOM
      if (userName === "Usuario" || !userName) {
        const userNameElement = document.querySelector(
          '[sec\\:authentication="name"]'
        );

        if (userNameElement) {
          userName = userNameElement.textContent || userNameElement.innerText;
        } else {
          // Buscar en el sidebar profile
          const sidebarName = document.querySelector(
            ".sidebar p.text-white.font-bold"
          );
          if (sidebarName) {
            userName = sidebarName.textContent.trim();
          }
        }
      }

      // Limpiar el rol para obtener solo el tipo (ADMIN, CLIENTE, etc.)
      if (userRole.startsWith("ROLE_")) {
        userRole = userRole.substring(5); // Remover 'ROLE_'
      }

      showNewWelcomeScreen(userName, userRole);

      sessionStorage.setItem("welcomeShown", "true");
      sessionStorage.setItem("splashShown", "true"); // Evitar que salga el otro splash
      sessionStorage.setItem("dashboardVisited", "true");

      // Limpiar URL
      if (isLoginSuccessParam) {
        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
      }
    }
  }
  */
  // CASO 2: Primera visita a la Home Page -> Mostrar Splash "Oasis Digital" (Letras)
  if (isHomePage && isFirstVisit) {
    showSplashScreen();
    sessionStorage.setItem("splashShown", "true");
  }

  // Función para mostrar el NUEVO Goodbye Screen (Power Off Effect)
  function showNewGoodbyeScreen() {
    const goodbyeScreen = document.createElement("div");
    goodbyeScreen.id = "oasis-goodbye-green";

    goodbyeScreen.innerHTML = `
            <div style="text-align: center; color: white; animation: fadeOutScale 0.8s ease-out;">
                <!-- Power Icon -->
                <div style="font-size: 5rem; color: #ef4444; margin-bottom: 1.5rem; filter: drop-shadow(0 0 20px rgba(239, 68, 68, 0.4)); animation: pulseRed 2s infinite;">
                    <i class="fas fa-power-off"></i>
                </div>

                <!-- Title -->
                <h1 style="font-size: 3.5rem; font-weight: 700; margin-bottom: 0.5rem; font-family: 'Inter', sans-serif;">Cerrando Sesión</h1>

                <!-- Message -->
                <p style="font-size: 1.2rem; color: #cbd5e1; margin-bottom: 2rem;">Gracias por usar Oasis Digital</p>

                <!-- Loading Bar -->
                <div style="width: 200px; height: 4px; background: rgba(255,255,255,0.1); border-radius: 2px; margin: 0 auto; overflow: hidden;">
                    <div style="width: 100%; height: 100%; background: #ef4444; animation: shrinkWidth 2s linear forwards;"></div>
                </div>
            </div>
        `;

    goodbyeScreen.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: #0f172a; /* Dark Background */
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 999999;
            animation: fadeIn 0.5s ease-out;
        `;

    document.body.style.overflow = "hidden";
    document.body.appendChild(goodbyeScreen);
  }

  // Agregar estilos extra para el goodbye screen
  const goodbyeStyle = document.createElement("style");
  goodbyeStyle.textContent = `
        @keyframes pulseRed {
            0%, 100% { transform: scale(1); opacity: 1; }
            50% { transform: scale(1.1); opacity: 0.8; }
        }
        @keyframes shrinkWidth {
            from { width: 100%; }
            to { width: 0%; }
        }
        @keyframes fadeOutScale {
            from { opacity: 0; transform: scale(0.9); }
            to { opacity: 1; transform: scale(1); }
        }
    `;
  document.head.appendChild(goodbyeStyle);

  // Detectar botones de cerrar sesión
  // Detectar botones de cerrar sesión
  /*
  const logoutButtons = document.querySelectorAll('button[type="submit"]');
  logoutButtons.forEach((button) => {
    const form = button.closest("form");
    if (
      form &&
      (form.action.includes("logout") || button.textContent.includes("Cerrar"))
    ) {
      button.addEventListener("click", function (e) {
        e.preventDefault(); // Detener el envío inmediato

        showNewGoodbyeScreen();

        // Limpiar sesión visual
        sessionStorage.removeItem("dashboardVisited");
        sessionStorage.removeItem("splashShown");
        sessionStorage.removeItem("welcomeShown");

        // Enviar formulario después de la animación
        setTimeout(() => {
          form.submit();
        }, 2500);
      });
    }
  });
  */

  // Efecto para alertas de éxito/error
  const alerts = document.querySelectorAll(".alert");
  alerts.forEach((alert) => {
    alert.classList.add("operation-effect");
  });

  // Efecto para botones al hacer clic (Green Ripple)
  const buttons = document.querySelectorAll(".btn");
  buttons.forEach((button) => {
    button.addEventListener("click", function (e) {
      // Si es el botón de logout, no mostrar ripple para no interferir
      if (this.textContent.includes("Cerrar")) return;

      const ripple = document.createElement("span");
      const rect = this.getBoundingClientRect();
      const size = Math.max(rect.width, rect.height);
      const x = e.clientX - rect.left - size / 2;
      const y = e.clientY - rect.top - size / 2;

      ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(16, 185, 129, 0.4); /* Green Ripple */
                border-radius: 50%;
                transform: scale(0);
                animation: rippleEffect 0.6s ease-out;
                pointer-events: none;
            `;

      if (!document.querySelector("#ripple-style")) {
        const rippleStyle = document.createElement("style");
        rippleStyle.id = "ripple-style";
        rippleStyle.textContent = `
                    @keyframes rippleEffect {
                        to { transform: scale(2); opacity: 0; }
                    }
                `;
        document.head.appendChild(rippleStyle);
      }

      this.style.position = "relative";
      this.style.overflow = "hidden";
      this.appendChild(ripple);

      setTimeout(() => {
        ripple.remove();
      }, 600);
    });
  });
});

// Efectos para operaciones específicas (Green Theme)
function showOperationEffect(type, message) {
  const effectDiv = document.createElement("div");
  effectDiv.className = "operation-notification";

  let icon = "";
  let color = "#10b981"; // Default Green

  switch (type) {
    case "bienvenida":
      icon = "fas fa-star";
      color = "#10b981"; // Green
      break;
    case "despedida":
      icon = "fas fa-hand-spock";
      color = "#f59e0b"; // Gold/Orange for goodbye
      break;
    case "reserva":
      icon = "fas fa-calendar-check";
      color = "#10b981";
      break;
    case "cancelar":
      icon = "fas fa-times-circle";
      color = "#ef4444"; // Red
      break;
    case "finalizar":
      icon = "fas fa-check-circle";
      color = "#10b981";
      break;
    case "habitacion":
      icon = "fas fa-bed";
      color = "#3b82f6"; // Blue (kept for distinction, but can be green)
      break;
    default:
      icon = "fas fa-check";
      color = "#10b981";
  }

  effectDiv.innerHTML = `
        <div class="operation-content">
            <i class="${icon} operation-icon"></i>
            <p class="operation-message">${message}</p>
        </div>
    `;

  effectDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, ${color}, ${color}dd);
        color: white;
        padding: 20px 25px;
        border-radius: 15px;
        z-index: 9998;
        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        animation: slideInRight 0.5s ease-out, slideOutRight 0.5s ease-in 3s forwards;
        min-width: 300px;
        border: 1px solid rgba(255,255,255,0.2);
    `;

  if (!document.querySelector("#operation-animations")) {
    const animationStyle = document.createElement("style");
    animationStyle.id = "operation-animations";
    animationStyle.textContent = `
            @keyframes slideInRight {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOutRight {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
            .operation-content { display: flex; align-items: center; gap: 15px; }
            .operation-icon { font-size: 1.5rem; animation: bounce 1s ease-in-out; }
            .operation-message { margin: 0; font-weight: 600; font-size: 1rem; }
            @keyframes bounce {
                0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
                40% { transform: translateY(-10px); }
                60% { transform: translateY(-5px); }
            }
        `;
    document.head.appendChild(animationStyle);
  }

  document.body.appendChild(effectDiv);

  setTimeout(() => {
    if (effectDiv.parentNode) {
      effectDiv.remove();
    }
  }, 4000);
}

// ============================================
// MODAL SYSTEM - Oasis Digital Green Theme
// ============================================

/**
 * Opens a modal by ID
 * @param {string} modalId - The ID of the modal to open
 */
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (!modal) {
    console.error(`Modal with ID "${modalId}" not found`);
    return;
  }

  modal.classList.remove("hidden");
  modal.classList.add("flex");
  document.body.style.overflow = "hidden";

  // Trigger animation
  setTimeout(() => {
    const content = modal.querySelector(".modal-content");
    if (content) {
      content.style.transform = "scale(1)";
      content.style.opacity = "1";
    }
  }, 10);
}

/**
 * Closes a modal by ID
 * @param {string} modalId - The ID of the modal to close
 */
function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (!modal) return;

  const content = modal.querySelector(".modal-content");
  if (content) {
    content.style.transform = "scale(0.95)";
    content.style.opacity = "0";
  }

  setTimeout(() => {
    modal.classList.add("hidden");
    modal.classList.remove("flex");
    document.body.style.overflow = "auto";
  }, 200);
}

/**
 * Closes all modals
 */
function closeAllModals() {
  const modals = document.querySelectorAll(".modal");
  modals.forEach((modal) => {
    closeModal(modal.id);
  });
}

// Initialize modal system on page load
document.addEventListener("DOMContentLoaded", function () {
  // Close modal when clicking backdrop
  document.querySelectorAll(".modal-backdrop").forEach((backdrop) => {
    backdrop.addEventListener("click", function (e) {
      if (e.target === this) {
        const modal = this.closest(".modal");
        if (modal) closeModal(modal.id);
      }
    });
  });

  // Close modal on ESC key
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
      closeAllModals();
    }
  });

  // Handle modal close buttons
  document.querySelectorAll("[data-modal-close]").forEach((button) => {
    button.addEventListener("click", function () {
      const modalId = this.getAttribute("data-modal-close");
      if (modalId) {
        closeModal(modalId);
      } else {
        const modal = this.closest(".modal");
        if (modal) closeModal(modal.id);
      }
    });
  });

  // Handle modal open buttons
  document.querySelectorAll("[data-modal-open]").forEach((button) => {
    button.addEventListener("click", function (e) {
      e.preventDefault();
      const modalId = this.getAttribute("data-modal-open");
      if (modalId) openModal(modalId);
    });
  });
});

// Export for global use
window.openModal = openModal;
window.closeModal = closeModal;
window.closeAllModals = closeAllModals;
