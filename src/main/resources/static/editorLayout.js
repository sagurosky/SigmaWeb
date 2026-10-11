paper.install(window);
const svg = document.getElementById('drawingArea');
const imgInput = document.getElementById('imgInput');

let mode = 'rect'; // 'rect', 'ellipse', 'polygon', 'edit'
let deleteMode = false;
let shape = null, startX = 0, startY = 0;
let selectedShape = null, activeHandle = null, isDragging = false;
let dragOffset = { x: 0, y: 0 }, backgroundImage = null, lastMergedId = null;

// Variables para la herramienta de polígono
let polygonPoints = [];
let tempPolygonElement = null;
let tempPreviewLine = null;
let activePolygonVerticesHandles = [];

// Lista de imágenes adjuntas por activo
let imagenesAdjuntas = [];

// Evento inicialización al cargar DOM
document.addEventListener("DOMContentLoaded", () => {
  setMode('rect');
  updateAssetList();
});

// Modos y navegación
function setMode(newMode) {
  mode = newMode;
  deleteMode = false;
  resetPolygonDrafting();
  clearHandles();
  updateButtonStyles();
  updateStatusText();
}

function toggleDeleteMode() {
  deleteMode = !deleteMode;
  if (deleteMode) mode = null;
  resetPolygonDrafting();
  clearHandles();
  updateButtonStyles();
  updateStatusText();
}

function resetPolygonDrafting() {
  polygonPoints = [];
  if (tempPolygonElement) {
    tempPolygonElement.remove();
    tempPolygonElement = null;
  }
  if (tempPreviewLine) {
    tempPreviewLine.remove();
    tempPreviewLine = null;
  }
}

function updateButtonStyles() {
  ['rectBtn', 'ellipseBtn', 'polygonBtn', 'editBtn', 'deleteBtn', 'imgBtn', 'saveSvgBtn', 'mergeBtn'].forEach(id => {
    const btn = document.getElementById(id);
    if (btn) btn.classList.remove('active');
  });
  if (mode === 'rect' && document.getElementById('rectBtn')) document.getElementById('rectBtn').classList.add('active');
  if (mode === 'ellipse' && document.getElementById('ellipseBtn')) document.getElementById('ellipseBtn').classList.add('active');
  if (mode === 'polygon' && document.getElementById('polygonBtn')) document.getElementById('polygonBtn').classList.add('active');
  if (mode === 'edit' && document.getElementById('editBtn')) document.getElementById('editBtn').classList.add('active');
  if (deleteMode && document.getElementById('deleteBtn')) document.getElementById('deleteBtn').classList.add('active');
}

function updateStatusText() {
  const statusEl = document.getElementById('editorStatusText');
  if (!statusEl) return;

  if (deleteMode) {
    statusEl.innerHTML = '<i class="fas fa-trash-alt me-1 text-danger"></i> <strong>Modo Eliminar:</strong> Haz clic en cualquier figura o activo conformados para eliminarlo.';
  } else if (mode === 'rect') {
    statusEl.innerHTML = '<i class="fas fa-vector-square me-1 text-primary"></i> <strong>Herramienta Rectángulo:</strong> Mantén presionado y arrastra en el área de dibujo para crear un rectángulo de boceto.';
  } else if (mode === 'ellipse') {
    statusEl.innerHTML = '<i class="fas fa-circle me-1 text-primary"></i> <strong>Herramienta Elipse:</strong> Mantén presionado y arrastra en el área de dibujo para crear una elipse de boceto.';
  } else if (mode === 'polygon') {
    statusEl.innerHTML = '<i class="fas fa-draw-polygon me-1 text-primary"></i> <strong>Herramienta Polígono:</strong> Haz clic para colocar vértices consecutivamente. Haz clic en el origen, doble clic o presiona Enter para cerrar el polígono.';
  } else if (mode === 'edit') {
    statusEl.innerHTML = '<i class="fas fa-mouse-pointer me-1 text-warning"></i> <strong>Modo Edición:</strong> Haz clic en una figura para seleccionar y mover sus vértices o ajustar sus dimensiones.';
  } else {
    statusEl.innerHTML = '<i class="fas fa-info-circle me-1"></i> Selecciona una herramienta para comenzar.';
  }
}

// Adjuntar imagen de fondo (layout de planta)
function triggerImage() {
  imgInput.click();
}

imgInput.addEventListener('change', e => {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = evt => {
    const href = evt.target.result;
    const tempImg = new Image();
    tempImg.onload = () => {
      const w = tempImg.naturalWidth || 900;
      const h = tempImg.naturalHeight || 600;
      svg.setAttribute('viewBox', `0 0 ${w} ${h}`);
      svg.setAttribute('width', w);
      svg.setAttribute('height', h);

      if (backgroundImage) backgroundImage.remove();
      const imgEl = document.createElementNS(svg.namespaceURI, 'image');
      imgEl.setAttributeNS('http://www.w3.org/1999/xlink', 'href', href);
      imgEl.setAttribute('x', 0);
      imgEl.setAttribute('y', 0);
      imgEl.setAttribute('width', w);
      imgEl.setAttribute('height', h);
      svg.prepend(imgEl);
      backgroundImage = imgEl;
    };
    tempImg.src = href;
  };
  reader.readAsDataURL(file);
  imgInput.value = '';
});

// Conversión precisa de coordenadas de pantalla a coordenadas del sistema SVG
function getSVGCoords(e) {
  const pt = svg.createSVGPoint();
  pt.x = e.clientX;
  pt.y = e.clientY;
  return pt.matrixTransform(svg.getScreenCTM().inverse());
}

// Generación y limpieza de Handles para edición
function createHandle(x, y, parent, vertexIndex = null) {
  const h = document.createElementNS(svg.namespaceURI, 'circle');
  h.setAttribute('cx', x);
  h.setAttribute('cy', y);
  h.setAttribute('r', 6);
  h.setAttribute('fill', '#2563eb');
  h.setAttribute('stroke', '#ffffff');
  h.setAttribute('stroke-width', '2');
  h.classList.add('handle');

  if (vertexIndex !== null) {
    h.setAttribute('data-vertex-index', vertexIndex);
  }

  h.addEventListener('mousedown', e => {
    e.stopPropagation();
    activeHandle = { shape: parent, vertexIndex: vertexIndex };
  });

  svg.appendChild(h);
  return h;
}

function clearHandles() {
  document.querySelectorAll('.handle').forEach(h => h.remove());
  activePolygonVerticesHandles = [];
}

function addHandles(el) {
  clearHandles();
  if (!el) return;

  if (el.tagName === 'rect') {
    const x = +el.getAttribute('x'), y = +el.getAttribute('y');
    const w = +el.getAttribute('width'), h = +el.getAttribute('height');
    createHandle(x + w, y + h, el);
  } else if (el.tagName === 'ellipse') {
    const cx = +el.getAttribute('cx'), cy = +el.getAttribute('cy');
    const rx = +el.getAttribute('rx'), ry = +el.getAttribute('ry');
    createHandle(cx + rx, cy + ry, el);
  } else if (el.tagName === 'polygon') {
    const pointsAttr = el.getAttribute('points');
    if (pointsAttr) {
      const points = parsePolygonPoints(pointsAttr);
      points.forEach((pt, idx) => {
        const h = createHandle(pt.x, pt.y, el, idx);
        activePolygonVerticesHandles.push(h);
      });
    }
  }
}

function parsePolygonPoints(pointsStr) {
  if (!pointsStr) return [];
  return pointsStr.trim().split(/\s+/).map(pair => {
    const [x, y] = pair.split(',').map(Number);
    return { x, y };
  });
}

function stringifyPolygonPoints(pointsArr) {
  return pointsArr.map(p => `${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ');
}

// Finalización y cierre de Polígono
function finishPolygon() {
  if (polygonPoints.length < 3) {
    resetPolygonDrafting();
    return;
  }

  const poly = document.createElementNS(svg.namespaceURI, 'polygon');
  poly.setAttribute('points', stringifyPolygonPoints(polygonPoints));
  poly.setAttribute('fill', 'rgba(37, 99, 235, 0.35)');
  poly.setAttribute('stroke', '#2563eb');
  poly.setAttribute('stroke-width', '2');
  poly.setAttribute('stroke-dasharray', '4 2');
  poly.classList.add('draft-shape');

  svg.appendChild(poly);
  resetPolygonDrafting();
}

// Eventos de teclado para Polígonos
window.addEventListener('keydown', e => {
  if (e.key === 'Enter' && mode === 'polygon') {
    finishPolygon();
  }
  if (e.key === 'Escape' && mode === 'polygon') {
    resetPolygonDrafting();
  }
  if ((e.key === 'Delete' || e.key === 'Backspace') && mode === 'edit' && selectedShape) {
    selectedShape.remove();
    clearHandles();
    selectedShape = null;
    updateAssetList();
  }
});

// Eventos de Mouse en Canvas SVG
svg.addEventListener('mousedown', e => {
  if (deleteMode) {
    if (e.target.tagName === 'path' || e.target.tagName === 'rect' || e.target.tagName === 'ellipse' || e.target.tagName === 'polygon') {
      e.target.remove();
      clearHandles();
      updateAssetList();
    }
    return;
  }

  if (activeHandle) return;
  const pt = getSVGCoords(e);

  // Modo Edición
  if (mode === 'edit') {
    if (e.target.tagName === 'rect' || e.target.tagName === 'ellipse' || e.target.tagName === 'polygon') {
      selectedShape = e.target;
      isDragging = true;
      addHandles(selectedShape);

      if (selectedShape.tagName === 'rect') {
        dragOffset.x = pt.x - (+selectedShape.getAttribute('x'));
        dragOffset.y = pt.y - (+selectedShape.getAttribute('y'));
      } else if (selectedShape.tagName === 'ellipse') {
        dragOffset.x = pt.x - (+selectedShape.getAttribute('cx'));
        dragOffset.y = pt.y - (+selectedShape.getAttribute('cy'));
      } else if (selectedShape.tagName === 'polygon') {
        dragOffset.x = pt.x;
        dragOffset.y = pt.y;
      }
      return;
    } else if (e.target.classList.contains('conformed-asset')) {
      selectedShape = e.target;
      clearHandles();
      return;
    }
  }

  // Herramienta Polígono
  if (mode === 'polygon') {
    // Si se hace clic cerca del primer punto (radio de 15px), cerramos el polígono
    if (polygonPoints.length >= 3) {
      const p0 = polygonPoints[0];
      const dist = Math.hypot(pt.x - p0.x, pt.y - p0.y);
      if (dist <= 15) {
        finishPolygon();
        return;
      }
    }

    polygonPoints.push({ x: pt.x, y: pt.y });

    if (!tempPolygonElement) {
      tempPolygonElement = document.createElementNS(svg.namespaceURI, 'polyline');
      tempPolygonElement.setAttribute('fill', 'none');
      tempPolygonElement.setAttribute('stroke', '#2563eb');
      tempPolygonElement.setAttribute('stroke-width', '2');
      tempPolygonElement.setAttribute('stroke-dasharray', '4 2');
      svg.appendChild(tempPolygonElement);
    }
    tempPolygonElement.setAttribute('points', stringifyPolygonPoints(polygonPoints));

    if (!tempPreviewLine) {
      tempPreviewLine = document.createElementNS(svg.namespaceURI, 'line');
      tempPreviewLine.setAttribute('stroke', 'rgba(37, 99, 235, 0.7)');
      tempPreviewLine.setAttribute('stroke-width', '1.5');
      tempPreviewLine.setAttribute('stroke-dasharray', '3 3');
      svg.appendChild(tempPreviewLine);
    }
    tempPreviewLine.setAttribute('x1', pt.x);
    tempPreviewLine.setAttribute('y1', pt.y);
    tempPreviewLine.setAttribute('x2', pt.x);
    tempPreviewLine.setAttribute('y2', pt.y);

    return;
  }

  // Herramientas Rectángulo y Elipse
  if (mode === 'rect' || mode === 'ellipse') {
    startX = pt.x;
    startY = pt.y;
    if (mode === 'rect') {
      shape = document.createElementNS(svg.namespaceURI, 'rect');
      shape.setAttribute('x', startX);
      shape.setAttribute('y', startY);
      shape.setAttribute('width', 0);
      shape.setAttribute('height', 0);
    } else {
      shape = document.createElementNS(svg.namespaceURI, 'ellipse');
      shape.setAttribute('cx', startX);
      shape.setAttribute('cy', startY);
      shape.setAttribute('rx', 0);
      shape.setAttribute('ry', 0);
    }
    // Estilos traslúcidos para bocetos
    shape.setAttribute('fill', 'rgba(37, 99, 235, 0.35)');
    shape.setAttribute('stroke', '#2563eb');
    shape.setAttribute('stroke-width', '2');
    shape.setAttribute('stroke-dasharray', '4 2');
    shape.classList.add('draft-shape');
    svg.appendChild(shape);
  }
});

// Doble clic para finalizar polígono
svg.addEventListener('dblclick', e => {
  if (mode === 'polygon') {
    e.preventDefault();
    finishPolygon();
  }
});

svg.addEventListener('mousemove', e => {
  const pt = getSVGCoords(e);

  // Arrastre de Handles en modo edición
  if (activeHandle) {
    const el = activeHandle.shape;
    if (el.tagName === 'rect') {
      const x = +el.getAttribute('x'), y = +el.getAttribute('y');
      el.setAttribute('width', Math.max(10, pt.x - x));
      el.setAttribute('height', Math.max(10, pt.y - y));
    } else if (el.tagName === 'ellipse') {
      const cx = +el.getAttribute('cx'), cy = +el.getAttribute('cy');
      el.setAttribute('rx', Math.abs(pt.x - cx));
      el.setAttribute('ry', Math.abs(pt.y - cy));
    } else if (el.tagName === 'polygon') {
      const vIdx = activeHandle.vertexIndex;
      if (vIdx !== null) {
        const points = parsePolygonPoints(el.getAttribute('points'));
        if (points[vIdx]) {
          points[vIdx].x = pt.x;
          points[vIdx].y = pt.y;
          el.setAttribute('points', stringifyPolygonPoints(points));
        }
      }
    }
    addHandles(el);
    return;
  }

  // Arrastre completo de figuras seleccionadas en modo edición
  if (isDragging && selectedShape) {
    if (selectedShape.tagName === 'rect') {
      selectedShape.setAttribute('x', pt.x - dragOffset.x);
      selectedShape.setAttribute('y', pt.y - dragOffset.y);
    } else if (selectedShape.tagName === 'ellipse') {
      selectedShape.setAttribute('cx', pt.x - dragOffset.x);
      selectedShape.setAttribute('cy', pt.y - dragOffset.y);
    } else if (selectedShape.tagName === 'polygon') {
      const dx = pt.x - dragOffset.x;
      const dy = pt.y - dragOffset.y;
      dragOffset.x = pt.x;
      dragOffset.y = pt.y;
      const points = parsePolygonPoints(selectedShape.getAttribute('points'));
      points.forEach(p => { p.x += dx; p.y += dy; });
      selectedShape.setAttribute('points', stringifyPolygonPoints(points));
    }
    addHandles(selectedShape);
    return;
  }

  // Vista previa de trazado de Polígono
  if (mode === 'polygon' && polygonPoints.length > 0 && tempPreviewLine) {
    const lastPt = polygonPoints[polygonPoints.length - 1];
    tempPreviewLine.setAttribute('x1', lastPt.x);
    tempPreviewLine.setAttribute('y1', lastPt.y);
    tempPreviewLine.setAttribute('x2', pt.x);
    tempPreviewLine.setAttribute('y2', pt.y);
    return;
  }

  // Dimensionamiento interactivo de Rectángulo y Elipse
  if (!shape) return;
  const w = pt.x - startX, h = pt.y - startY;
  if (mode === 'rect') {
    shape.setAttribute('width', Math.abs(w));
    shape.setAttribute('height', Math.abs(h));
    if (w < 0) shape.setAttribute('x', startX + w);
    if (h < 0) shape.setAttribute('y', startY + h);
  } else if (mode === 'ellipse') {
    shape.setAttribute('rx', Math.abs(w / 2));
    shape.setAttribute('ry', Math.abs(h / 2));
    shape.setAttribute('cx', startX + w / 2);
    shape.setAttribute('cy', startY + h / 2);
  }
});

svg.addEventListener('mouseup', () => {
  shape = null;
  isDragging = false;
  activeHandle = null;
});

// Conformación de Figuras con Modal SweetAlert2 y Paper.js
function confirmMerge() {
  const draftShapes = svg.querySelectorAll('rect, ellipse, polygon, .draft-shape');
  if (draftShapes.length === 0) {
    Swal.fire('Atención', 'Dibuja al menos una figura (rectángulo, elipse o polígono) antes de conformar el activo.', 'info');
    return;
  }

  Swal.fire({
    title: 'Conformar activo',
    html: `
      <div style="display: flex; flex-direction: column; align-items: center; gap: 12px; text-align: left;">
        <div style="width: 100%;">
          <label for="nombreActivo" style="font-weight: 600; font-size: 0.9rem; margin-bottom: 4px; display: block;">Nombre único del Activo:</label>
          <input type="text" id="nombreActivo" class="swal2-input" style="width: 100%; margin: 0;" placeholder="Ej: Prensa-01">
        </div>
        <div style="width: 100%;">
          <label style="font-weight: 600; font-size: 0.9rem; margin-bottom: 4px; display: block;">Imagen del Activo (Opcional):</label>
          <input type="file" id="imagenActivo" class="swal2-file" accept="image/png, image/jpeg, image/jpg" style="display: none;">
          <button id="btnSeleccionarImagen" class="btn btn-outline-primary w-100" type="button">
            <i class="fas fa-upload me-1"></i> Seleccionar foto
          </button>
          <div id="nombreArchivo" style="margin-top: 6px; font-size: 13px; color: var(--text-muted, #64748b); text-align: center;">No seleccionada</div>
        </div>
      </div>
    `,
    showCancelButton: true,
    confirmButtonText: '<i class="fas fa-check-circle me-1"></i> Conformar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#2563eb',
    didOpen: () => {
      document.getElementById('btnSeleccionarImagen').addEventListener('click', () => {
        document.getElementById('imagenActivo').click();
      });

      document.getElementById('imagenActivo').addEventListener('change', (event) => {
        const archivo = event.target.files[0];
        if (archivo) {
          const formatosPermitidos = ['image/png', 'image/jpeg', 'image/jpg'];
          if (!formatosPermitidos.includes(archivo.type)) {
            Swal.fire('Error', 'Formato no válido. Solo se permiten PNG y JPG.', 'error');
            document.getElementById('imagenActivo').value = '';
            document.getElementById('nombreArchivo').innerText = 'No seleccionada';
            return;
          }
          document.getElementById('nombreArchivo').innerText = archivo.name;
        }
      });
    },
    preConfirm: () => {
      const idName = document.getElementById('nombreActivo').value.trim();
      const archivo = document.getElementById('imagenActivo').files[0];

      if (!idName) {
        Swal.showValidationMessage('El nombre no puede estar vacío');
        return false;
      }

      if (archivo) {
        const nuevoNombre = `${idName}.${archivo.name.split('.').pop()}`;
        const nuevoArchivo = new File([archivo], nuevoNombre, { type: archivo.type });
        imagenesAdjuntas.push({ id: idName, archivo: nuevoArchivo });
      }

      mergeShapes(idName);
      return { idName, archivo };
    }
  });
}

function mergeShapes(idName) {
  const canvas = document.createElement('canvas');
  paper.setup(canvas);
  let paths = [];

  svg.querySelectorAll('rect, ellipse, polygon, .draft-shape').forEach(el => {
    let p;
    if (el.tagName === 'rect') {
      const x = +el.getAttribute('x'), y = +el.getAttribute('y');
      const w = +el.getAttribute('width'), h = +el.getAttribute('height');
      p = new paper.Path.Rectangle(new paper.Rectangle(x, y, w, h));
    } else if (el.tagName === 'ellipse') {
      const cx = +el.getAttribute('cx'), cy = +el.getAttribute('cy');
      const rx = +el.getAttribute('rx'), ry = +el.getAttribute('ry');
      p = new paper.Path.Ellipse({ center: [cx, cy], radius: [rx, ry] });
    } else if (el.tagName === 'polygon') {
      const points = parsePolygonPoints(el.getAttribute('points'));
      const paperPoints = points.map(pt => new paper.Point(pt.x, pt.y));
      p = new paper.Path({ segments: paperPoints, closed: true });
    }
    if (p) paths.push(p);
  });

  if (paths.length === 0) return;

  let result = paths[0];
  for (let i = 1; i < paths.length; i++) {
    result = result.unite(paths[i]);
  }

  const svgStr = result.exportSVG({ asString: true });
  const newPath = new DOMParser().parseFromString(svgStr, 'image/svg+xml').documentElement;

  // Estilo traslúcido verde para Activos Conformados
  newPath.setAttribute('fill', 'rgba(16, 185, 129, 0.35)');
  newPath.setAttribute('stroke', '#059669');
  newPath.setAttribute('stroke-width', '1.5');
  newPath.classList.add('conformed-asset');

  if (idName) newPath.setAttribute('id', idName);

  clearHandles();
  svg.querySelectorAll('rect, ellipse, polygon, .draft-shape').forEach(el => el.remove());
  svg.appendChild(newPath);

  paper.project.clear();
  lastMergedId = idName;
  updateButtonStyles();
  updateAssetList();
}

// Actualización del Panel Lateral de Activos Conformados
function updateAssetList() {
  const container = document.getElementById('listaActivosConformados');
  if (!container) return;

  const assets = svg.querySelectorAll('path.conformed-asset, path[id]');
  container.innerHTML = '';

  const badgeCount = document.getElementById('conformedCountBadge');
  if (badgeCount) badgeCount.textContent = assets.length;

  if (assets.length === 0) {
    container.innerHTML = '<div class="text-center text-muted p-3 small">No se han conformados activos aún.</div>';
    return;
  }

  assets.forEach(asset => {
    const id = asset.getAttribute('id') || 'Sin ID';
    const hasImage = imagenesAdjuntas.some(img => img.id === id);

    const item = document.createElement('div');
    item.className = 'asset-list-item d-flex align-items-center justify-content-between p-2 mb-2 rounded border bg-surface-elevated';
    item.innerHTML = `
      <div class="d-flex align-items-center gap-2 text-truncate me-2">
        <i class="fas fa-cube text-success"></i>
        <span class="fw-bold small text-truncate">${id}</span>
        ${hasImage ? '<span class="badge bg-info-subtle text-info text-xs" title="Imagen cargada"><i class="fas fa-image"></i></span>' : ''}
      </div>
      <button class="btn btn-outline-danger btn-xs btn-del-asset" title="Eliminar este activo">
        <i class="fas fa-trash-alt"></i>
      </button>
    `;

    item.addEventListener('mouseenter', () => {
      asset.style.fill = 'rgba(16, 185, 129, 0.7)';
      asset.style.strokeWidth = '2.5';
    });

    item.addEventListener('mouseleave', () => {
      asset.style.fill = 'rgba(16, 185, 129, 0.35)';
      asset.style.strokeWidth = '1.5';
    });

    item.querySelector('.btn-del-asset').addEventListener('click', (e) => {
      e.stopPropagation();
      Swal.fire({
        title: `¿Eliminar activo "${id}"?`,
        text: "Se eliminará la representación del activo y su imagen asociada.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Eliminar",
        cancelButtonText: "Cancelar",
        confirmButtonColor: "#dc2626"
      }).then((result) => {
        if (result.isConfirmed) {
          asset.remove();
          imagenesAdjuntas = imagenesAdjuntas.filter(img => img.id !== id);
          clearHandles();
          updateAssetList();
        }
      });
    });

    container.appendChild(item);
  });
}

// Enviar SVG e Imágenes al Servidor
async function sendSvgToServer() {
  const nombre = document.getElementById("nombre").value.trim();

  if (!nombre) {
    Swal.fire('Campo requerido', 'Por favor ingresa un nombre para el layout.', 'warning');
    return;
  }

  const conformedPaths = svg.querySelectorAll('path[id]');
  if (conformedPaths.length === 0) {
    Swal.fire('Sin activos conformados', 'Debe conformar al menos un activo antes de guardar el layout en el servidor.', 'warning');
    return;
  }

  const endpoint = '/inicio/guardarSvg';
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  const clone = svg.cloneNode(true);

  // Limpieza de borradores y preparación de anchors para dynamic links
  clone.querySelectorAll('.draft-shape, .handle, line, polyline').forEach(el => el.remove());

  clone.querySelectorAll('path').forEach(path => {
    let id = path.getAttribute('id') || '';
    path.setAttribute("id", id);

    const anchor = document.createElementNS(svg.namespaceURI, 'a');
    anchor.setAttribute('href', `/inicio/activo/${toCamelCase(id)}`);
    path.parentNode.replaceChild(anchor, path);

    if (!path.hasAttribute('data-estado')) {
      path.setAttribute('data-estado', 'operativa');
    }
    anchor.appendChild(path);
  });

  // Remover propiedad inline fill para que el parpadeo dynamic CSS de plantillas funcione
  clone.querySelectorAll('path').forEach(path => {
    path.removeAttribute('fill');
    path.removeAttribute('stroke');
    path.removeAttribute('stroke-width');
    path.removeAttribute('class');
  });

  clone.querySelectorAll('image').forEach(img => {
    const xlinkHref = img.getAttributeNS("http://www.w3.org/1999/xlink", "href");
    if (xlinkHref) {
      img.removeAttributeNS("http://www.w3.org/1999/xlink", "href");
      img.setAttribute("href", xlinkHref);
    }
  });

  // Ajuste responsivo de la imagen SVG
  const currentViewBox = svg.getAttribute("viewBox") || `0 0 ${svg.getAttribute('width') || 900} ${svg.getAttribute('height') || 600}`;
  clone.setAttribute("width", "100%");
  clone.removeAttribute("height");
  clone.setAttribute("viewBox", currentViewBox);
  clone.setAttribute("preserveAspectRatio", "xMidYMid meet");
  clone.style.width = "100%";
  clone.style.height = "auto";
  clone.style.maxHeight = "85vh";
  clone.style.display = "block";

  let source = new XMLSerializer().serializeToString(clone);

  try {
    const formData = new FormData();
    formData.append('svg', new Blob([source], { type: 'image/svg+xml' }));
    formData.append("nombre", nombre);

    imagenesAdjuntas.forEach(item => {
      formData.append(item.id, item.archivo);
    });

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        [csrfHeader]: csrfToken
      },
      body: formData
    });

    if (response.ok) {
      Swal.fire({
        title: 'Guardado correctamente',
        text: 'El layout y sus activos fueron enviados e integrados al servidor.',
        icon: 'success',
        confirmButtonText: 'Aceptar'
      }).then(() => {
        window.location.href = '/inicio/crearLayout';
      });
    } else {
      Swal.fire('Error', 'Ocurrió un problema al guardar en el servidor.', 'error');
    }
  } catch (err) {
    console.error('Fetch falló:', err);
    Swal.fire('Error', 'No se pudo conectar con el servidor.', 'error');
  }
}

function toCamelCase(str) {
  return str
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9\s]/g, '')
    .toLowerCase()
    .replace(/(?:^\w|[A-Z]|\b\w)/g, (word, index) =>
      index === 0 ? word.toLowerCase() : word.toUpperCase()
    )
    .replace(/\s+/g, '');
}