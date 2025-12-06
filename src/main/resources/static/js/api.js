// Configuración global de la aplicación
const AppConfig = {
  apiKeys: {
    openweather: "demo", // Configurar en producción
    unsplash: "demo", // Configurar en producción
    dniPeru: "demo", // Configurar en producción
  },
  roles: {
    CLIENTE: "cliente",
    RECEPCIONISTA: "recepcionista",
    ADMIN: "admin",
  },
};

// Usuario actual
let currentUser = {
  role: localStorage.getItem("userRole") || "cliente",
  name: localStorage.getItem("userName") || "Usuario",
  id: localStorage.getItem("userId") || "1",
};

// Actualizar navbar según rol
function updateNavbarByRole() {
  const navbarNav = document.getElementById("navbarNav");
  if (!navbarNav) return;

  const reservarLink = navbarNav.querySelector('a[href="reservar.html"]');
  if (reservarLink) {
    reservarLink.parentElement.style.display = "none";
  }
}

// Establecer rol de usuario
function setUserRole(role, name = "Usuario") {
  currentUser.role = role;
  currentUser.name = name;
  localStorage.setItem("userRole", role);
  localStorage.setItem("userName", name);
  updateNavbarByRole();
}

// Obtener datos del clima
async function fetchWeather(city = "Lima") {
  if (!city || typeof city !== "string") {
    city = "Lima";
  }

  // Sanitizar entrada
  city = encodeURIComponent(city.trim());

  try {
    const apiKey = AppConfig.apiKeys.openweather;
    if (!apiKey || apiKey === "demo") {
      throw new Error("API key no configurada");
    }

    const response = await fetch(
      `https://api.openweathermap.org/data/2.5/weather?q=${city}&appid=${apiKey}&units=metric&lang=es`
    );

    if (!response.ok) {
      throw new Error(`Error HTTP: ${response.status}`);
    }

    const data = await response.json();
    return {
      temp: Math.round(data.main.temp),
      description: data.weather[0].description,
      icon: data.weather[0].icon,
      humidity: data.main.humidity,
      windSpeed: Math.round(data.wind.speed * 3.6), // convertir m/s a km/h
    };
  } catch (error) {
    console.error("Error fetching weather:", error);
    // Retornar datos de ejemplo si falla la API
    return {
      temp: 22,
      description: "Clima agradable",
      icon: "01d",
      humidity: 65,
      windSpeed: 15,
    };
  }
}

// Obtener imágenes (usar imágenes locales del proyecto como primera opción)
async function fetchUnsplashImages(query = "luxury hotel", count = 6) {
  // Lista de imágenes locales (asegúrate de que existan en /static/images)
  const localImages = [
    "/images/img.jpg",
    "/images/img2.jpg",
    "/images/img3.jpg",
    "/images/img4.jpg",
    "/images/img5.jpg",
    "/images/img6.jpg",
    "/images/img7.jpg",
    "/images/img8.jpg",
  ];

  // Si la cantidad solicitada está cubierta por las locales, retornar slice
  if (count <= localImages.length) {
    return localImages.slice(0, count);
  }

  // Si piden más, combinar locales + fallback remoto
  const fallback = [
    "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
    "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800",
    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",
    "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800",
    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800",
    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800",
  ];

  return localImages.concat(fallback).slice(0, count);
}

// Validar DNI (API de Perú)
async function validateDNI(dni) {
  try {
    // En producción usar token real
    const token = AppConfig.apiKeys.dniPeru;
    const response = await fetch(
      `https://dniruc.apisperu.com/api/v1/dni/${dni}?token=${token}`
    );

    if (!response.ok) {
      throw new Error("Error al validar DNI");
    }

    const data = await response.json();
    return {
      success: true,
      nombres: data.nombres || "",
      apellidoPaterno: data.apellidoPaterno || "",
      apellidoMaterno: data.apellidoMaterno || "",
    };
  } catch (error) {
    console.error("Error validating DNI:", error);
    // Retornar datos de ejemplo para desarrollo
    return {
      success: false,
      nombres: "",
      apellidoPaterno: "",
      apellidoMaterno: "",
    };
  }
}

// Formatear fecha
function formatDate(date) {
  const options = { year: "numeric", month: "long", day: "numeric" };
  return new Date(date).toLocaleDateString("es-ES", options);
}

// Formatear moneda
function formatCurrency(amount) {
  return new Intl.NumberFormat("es-PE", {
    style: "currency",
    currency: "PEN",
  }).format(amount);
}

// Mostrar notificaciones
function showNotification(message, type = "info") {
  if (!message || typeof message !== "string") {
    return;
  }

  // Mapeo de tipos
  const typeMap = {
    success: "success",
    error: "danger",
    warning: "warning",
    info: "info",
  };

  const alertType = typeMap[type] || "info";

  const alertDiv = document.createElement("div");
  alertDiv.className = `alert alert-${alertType} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3`;
  alertDiv.style.zIndex = "9999";
  alertDiv.style.minWidth = "300px";

  // Usar textContent para prevenir XSS
  const messageSpan = document.createElement("span");
  messageSpan.textContent = message;

  const closeButton = document.createElement("button");
  closeButton.type = "button";
  closeButton.className = "btn-close";
  closeButton.setAttribute("data-bs-dismiss", "alert");

  alertDiv.appendChild(messageSpan);
  alertDiv.appendChild(closeButton);
  document.body.appendChild(alertDiv);

  // Auto-remover después de 5 segundos
  setTimeout(() => {
    alertDiv.classList.remove("show");
    setTimeout(() => alertDiv.remove(), 150);
  }, 5000);
}

// Generar datos mock para desarrollo
function generateMockData() {
  return {
    reservas: [
      {
        id: 1,
        habitacion: "101",
        tipo: "Suite Presidencial",
        checkIn: "2025-10-15",
        checkOut: "2025-10-18",
        estado: "Confirmada",
        precio: 450,
      },
      {
        id: 2,
        habitacion: "205",
        tipo: "Habitación Doble",
        checkIn: "2025-10-20",
        checkOut: "2025-10-22",
        estado: "Pendiente",
        precio: 280,
      },
      {
        id: 3,
        habitacion: "312",
        tipo: "Suite Junior",
        checkIn: "2025-11-01",
        checkOut: "2025-11-05",
        estado: "Confirmada",
        precio: 350,
      },
    ],
    habitaciones: [
      {
        id: 101,
        tipo: "Suite Presidencial",
        estado: "Disponible",
        precio: 450,
        capacidad: 2,
        amenidades: ["Jacuzzi", "Vista al mar", "Sala de estar"],
      },
      {
        id: 102,
        tipo: "Suite Presidencial",
        estado: "Ocupada",
        precio: 450,
        capacidad: 2,
        amenidades: ["Jacuzzi", "Vista al mar", "Sala de estar"],
      },
      {
        id: 201,
        tipo: "Habitación Doble",
        estado: "Disponible",
        precio: 280,
        capacidad: 2,
        amenidades: ["TV", "WiFi", "Minibar"],
      },
      {
        id: 202,
        tipo: "Habitación Doble",
        estado: "Disponible",
        precio: 280,
        capacidad: 2,
        amenidades: ["TV", "WiFi", "Minibar"],
      },
      {
        id: 203,
        tipo: "Habitación Doble",
        estado: "Ocupada",
        precio: 280,
        capacidad: 2,
        amenidades: ["TV", "WiFi", "Minibar"],
      },
      {
        id: 301,
        tipo: "Suite Junior",
        estado: "Mantenimiento",
        precio: 350,
        capacidad: 3,
        amenidades: ["Balcón", "Cocina", "WiFi"],
      },
      {
        id: 302,
        tipo: "Suite Junior",
        estado: "Disponible",
        precio: 350,
        capacidad: 3,
        amenidades: ["Balcón", "Cocina", "WiFi"],
      },
      {
        id: 108,
        tipo: "Habitación Simple",
        estado: "Disponible",
        precio: 180,
        capacidad: 1,
        amenidades: ["TV", "WiFi"],
      },
      {
        id: 109,
        tipo: "Habitación Simple",
        estado: "Disponible",
        precio: 180,
        capacidad: 1,
        amenidades: ["TV", "WiFi"],
      },
    ],
    checkIns: [
      {
        id: 1,
        cliente: "Juan Pérez",
        habitacion: "101",
        hora: "14:00",
        dni: "12345678",
      },
      {
        id: 2,
        cliente: "María García",
        habitacion: "205",
        hora: "15:30",
        dni: "87654321",
      },
    ],
    checkOuts: [
      {
        id: 1,
        cliente: "Carlos López",
        habitacion: "312",
        hora: "11:00",
        dni: "45678912",
      },
    ],
    ingresos: {
      total: 125500,
      mes: 45300,
      dia: 2800,
    },
    ocupacion: {
      porcentaje: 78,
      ocupadas: 28,
      disponibles: 8,
      total: 36,
    },
    empleados: 24,
    servicios: [
      { id: 1, nombre: "Spa & Wellness", precio: 120, disponible: true },
      { id: 2, nombre: "Restaurant Gourmet", precio: 80, disponible: true },
      { id: 3, nombre: "Room Service", precio: 50, disponible: true },
      { id: 4, nombre: "Lavandería", precio: 35, disponible: true },
      { id: 5, nombre: "Bar Premium", precio: 45, disponible: true },
      { id: 6, nombre: "Transporte VIP", precio: 80, disponible: true },
    ],
  };
}

// Scroll suave para links internos
function initSmoothScroll() {
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      const href = this.getAttribute("href");
      if (href === "#") return;

      e.preventDefault();
      const target = document.querySelector(href);
      if (target) {
        target.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      }
    });
  });
}

// API Helper Functions
const API = {
  baseURL: window.location.origin + "/api",

  async request(endpoint, options = {}) {
    if (!endpoint || typeof endpoint !== "string") {
      throw new Error("Endpoint inválido");
    }

    const token = localStorage.getItem("jwt_token");
    const csrfToken = document
      .querySelector('meta[name="_csrf"]')
      ?.getAttribute("content");

    const headers = {
      "Content-Type": "application/json",
      ...options.headers,
    };

    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    if (csrfToken) {
      headers["X-CSRF-TOKEN"] = csrfToken;
    }

    try {
      const response = await fetch(`${this.baseURL}${endpoint}`, {
        ...options,
        headers,
      });

      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.error || "Error en la petición");
      }

      return await response.json();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: "GET" });
  },

  post(endpoint, data) {
    return this.request(endpoint, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  put(endpoint, data) {
    return this.request(endpoint, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: "DELETE" });
  },
};

// Funciones de Cliente
async function registrarClienteAPI(datos) {
  try {
    const response = await API.post("/clientes", datos);
    showNotification("Cliente registrado exitosamente", "success");
    return response;
  } catch (error) {
    showNotification(error.message, "error");
    throw error;
  }
}

async function buscarClientePorDNI(dni) {
  try {
    return await API.get(`/clientes/dni/${dni}`);
  } catch (error) {
    showNotification("Cliente no encontrado", "error");
    throw error;
  }
}

// Funciones de Reserva
async function crearReservaAPI(datos) {
  try {
    const response = await API.post("/reservas", datos);
    showNotification("Reserva creada exitosamente", "success");
    return response;
  } catch (error) {
    showNotification(error.message, "error");
    throw error;
  }
}

async function obtenerHabitacionesDisponibles(fechaInicio, fechaFin) {
  try {
    return await API.get(
      `/habitaciones/disponibles?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`
    );
  } catch (error) {
    console.error("Error obteniendo habitaciones:", error);
    return [];
  }
}

// Funciones de Habitación
async function obtenerHabitaciones() {
  try {
    return await API.get("/habitaciones");
  } catch (error) {
    console.error("Error obteniendo habitaciones:", error);
    return [];
  }
}

// Inicializar la aplicación
document.addEventListener("DOMContentLoaded", function () {
  // Actualizar navbar según rol
  updateNavbarByRole();

  // Actualizar nombre de usuario si existe el elemento
  const userNameElement = document.getElementById("userName");
  if (userNameElement) {
    userNameElement.textContent = currentUser.name;
  }

  // Inicializar scroll suave
  initSmoothScroll();

  // Log de inicio (solo en desarrollo)
  console.log("Oasis Digital - Sistema Hotelero");
  console.log(
    "Usuario actual:",
    currentUser.name,
    "(" + currentUser.role + ")"
  );
});
