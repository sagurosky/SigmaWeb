# Contexto para AI

## Proyecto
Aplicación web para gestión de mantenimiento de fabricas

## Arquitectura
- Frontend: html, css, js, bootstrap, jquery, thymeleaf
- Backend: java 17 springboot
- Base de datos: mysql

## Decisiones importantes
- el proyecto es grande, debo cuidar los tokens por lo que trabajamos modularmente
- los modulos son (de acuerdo a su funcionalidad):
    - login, usuarios, permisos
    - generados de layouts
    - gestión de paradas de maquinas (activos) desde su notificación hasta el cierre de las mismas (liberación) recolectando datos en tiemo real para los calculos de indicadores (kpi a futuro) y evaluacion de desempeño tecnico.
    - generador de informes
    - preventivos y mejoras
    - indicadores y estadisticas

## Estado actual
- Orden, limpieza en el codigo, optimización de código, revision de configuración y seguridades, etc

## Próximos pasos
- mejorar frontend de todas las pantallas manteniendo estilo en todas ellas