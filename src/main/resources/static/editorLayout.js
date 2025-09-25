  paper.install(window);
 const svg = document.getElementById('drawingArea');
 const imgInput = document.getElementById('imgInput');

 let mode = 'rect';
 let deleteMode = false;
 let shape = null, startX = 0, startY = 0;
 let selectedShape = null, activeHandle = null, isDragging = false;
 let dragOffset = { x: 0, y: 0 }, backgroundImage = null, lastMergedId = null;

 // Modos y estilos
 function setMode(newMode) {
   mode = newMode; deleteMode = false;
   clearHandles(); updateButtonStyles();
 }
 function toggleDeleteMode() {
   deleteMode = !deleteMode; if (deleteMode) mode = null;
   clearHandles(); updateButtonStyles();
 }
 function updateButtonStyles() {
   ['rectBtn','ellipseBtn','editBtn','deleteBtn','imgBtn','saveSvgBtn','mergeBtn'].forEach(id => {
     const btn = document.getElementById(id);
     if (btn) btn.classList.remove('active');
   });
   if (mode==='rect') document.getElementById('rectBtn').classList.add('active');
   if (mode==='ellipse') document.getElementById('ellipseBtn').classList.add('active');
   if (mode==='edit') document.getElementById('editBtn').classList.add('active');
   if (deleteMode) document.getElementById('deleteBtn').classList.add('active');
 }

 // Adjuntar imagen
 function triggerImage() { imgInput.click(); }
 imgInput.addEventListener('change', e => {
   const file = e.target.files[0]; if (!file) return;
   const reader = new FileReader();
   reader.onload = evt => {
     const href = evt.target.result;
     if (backgroundImage) backgroundImage.remove();
     const imgEl = document.createElementNS(svg.namespaceURI, 'image');
     imgEl.setAttributeNS('http://www.w3.org/1999/xlink','href', href);
     imgEl.setAttribute('x',0); imgEl.setAttribute('y',0);
     imgEl.setAttribute('width', svg.getAttribute('width'));
     imgEl.setAttribute('height', svg.getAttribute('height'));
     svg.prepend(imgEl); backgroundImage = imgEl;
   };
   reader.readAsDataURL(file); imgInput.value='';
 });

 // Handles y creación
 function createHandle(x,y,parent) {
   const h = document.createElementNS(svg.namespaceURI,'circle');
   h.setAttribute('cx',x); h.setAttribute('cy',y); h.setAttribute('r',5);
   h.classList.add('handle');
   h.addEventListener('mousedown', e => { e.stopPropagation(); activeHandle={shape:parent}; });
   svg.appendChild(h); return h;
 }
 function clearHandles() { document.querySelectorAll('.handle').forEach(h=>h.remove()); }
 function addHandles(el) {
   clearHandles();
   if (el.tagName==='rect') {
     const x=+el.getAttribute('x'), y=+el.getAttribute('y');
     const w=+el.getAttribute('width'), h=+el.getAttribute('height');
     createHandle(x+w,y+h,el);
   } else if (el.tagName==='ellipse') {
     const cx=+el.getAttribute('cx'), cy=+el.getAttribute('cy');
     const rx=+el.getAttribute('rx'), ry=+el.getAttribute('ry');
     createHandle(cx+rx,cy+ry,el);
   }
 }

 // Eventos de mouse
 svg.addEventListener('mousedown', e=>{
  // if (deleteMode && e.target.tagName==='path') { e.target.remove(); return; }
   if (deleteMode && (e.target.tagName==='path'||e.target.tagName==='rect'||e.target.tagName==='ellipse')) { e.target.remove(); return; }
   if (activeHandle) return;
   if (mode==='edit' && (e.target.tagName==='rect'||e.target.tagName==='ellipse')) {
     selectedShape=e.target; isDragging=true; addHandles(selectedShape);
     if (selectedShape.tagName==='rect'){
       dragOffset.x=e.offsetX-+selectedShape.getAttribute('x');
       dragOffset.y=e.offsetY-+selectedShape.getAttribute('y');
     } else {
       dragOffset.x=e.offsetX-+selectedShape.getAttribute('cx');
       dragOffset.y=e.offsetY-+selectedShape.getAttribute('cy');
     }
     return;
   }
   if (!mode) return;
   startX=e.offsetX; startY=e.offsetY;
   if (mode==='rect'){
     shape=document.createElementNS(svg.namespaceURI,'rect');
     shape.setAttribute('x',startX); shape.setAttribute('y',startY);
     shape.setAttribute('width',0); shape.setAttribute('height',0);
   } else {
     shape=document.createElementNS(svg.namespaceURI,'ellipse');
     shape.setAttribute('cx',startX); shape.setAttribute('cy',startY);
     shape.setAttribute('rx',0); shape.setAttribute('ry',0);
   }
   shape.setAttribute('fill','rgb(173,216,230)');
   shape.setAttribute('stroke','none');
   svg.appendChild(shape);
 });

 svg.addEventListener('mousemove', e=>{
   if (activeHandle) {
     const el=activeHandle.shape;
     if (el.tagName==='rect'){
       const x=+el.getAttribute('x'), y=+el.getAttribute('y');
       el.setAttribute('width',Math.max(10,e.offsetX-x));
       el.setAttribute('height',Math.max(10,e.offsetY-y));
     } else {
       const cx=+el.getAttribute('cx'), cy=+el.getAttribute('cy');
       el.setAttribute('rx',Math.abs(e.offsetX-cx));
       el.setAttribute('ry',Math.abs(e.offsetY-cy));
     }
     addHandles(el); return;
   }
   if (isDragging && selectedShape) {
     if(selectedShape.tagName==='rect'){
       selectedShape.setAttribute('x',e.offsetX-dragOffset.x);
       selectedShape.setAttribute('y',e.offsetY-dragOffset.y);
     } else {
       selectedShape.setAttribute('cx',e.offsetX-dragOffset.x);
       selectedShape.setAttribute('cy',e.offsetY-dragOffset.y);
     }
     addHandles(selectedShape); return;
   }
   if (!shape) return;
   const w=e.offsetX-startX, h=e.offsetY-startY;
   if(mode==='rect'){
     shape.setAttribute('width',Math.abs(w)); shape.setAttribute('height',Math.abs(h));
     if(w<0) shape.setAttribute('x',startX+w);
     if(h<0) shape.setAttribute('y',startY+h);
   } else {
     shape.setAttribute('rx',Math.abs(w/2)); shape.setAttribute('ry',Math.abs(h/2));
     shape.setAttribute('cx',startX+w/2); shape.setAttribute('cy',startY+h/2);
   }
 });
 svg.addEventListener('mouseup', ()=>{ shape=null; isDragging=false; activeHandle=null; });

 // Merge figuras
 let imagenesAdjuntas = [];

 function confirmMerge() {
     Swal.fire({
         title: 'Conformar activo',
         html: `
             <div style="display: flex; flex-direction: column; align-items: center; gap: 10px;">
                 <label for="nombreActivo">Nombre:</label>
                 <input type="text" id="nombreActivo" class="swal2-input" placeholder="nombre único">

                 <label for="imagenActivo">Imagen:</label>
                 <input type="file" id="imagenActivo" class="swal2-file" accept="image/png, image/jpeg, image/jpg" style="display: none;">
                 <button id="btnSeleccionarImagen" class="swal2-confirm swal2-styled">
                     Seleccionar imagen
                 </button>
                 <span id="nombreArchivo" style="margin-top: 5px; font-size: 14px;">No seleccionado</span>
             </div>
         `,
         showCancelButton: true,
         confirmButtonText: 'Conformar',
         cancelButtonText: 'Cancelar',
         didOpen: () => {
             document.getElementById('btnSeleccionarImagen').addEventListener('click', () => {
                 document.getElementById('imagenActivo').click();
             });

             document.getElementById('imagenActivo').addEventListener('change', (event) => {
                 const archivo = event.target.files[0];
                 if (archivo) {
                     const formatosPermitidos = ['image/png', 'image/jpeg', 'image/jpg'];
                     if (!formatosPermitidos.includes(archivo.type)) {
                         Swal.fire('Error', 'Formato de imagen no válido. Solo se permiten PNG y JPG.', 'error');
                         document.getElementById('imagenActivo').value = '';
                         document.getElementById('nombreArchivo').innerText = 'No seleccionado';
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
                 // Crear un nuevo archivo con el nombre idName.extensión_original
                 const nuevoNombre = `${idName}.${archivo.name.split('.').pop()}`;
                 const nuevoArchivo = new File([archivo], nuevoNombre, { type: archivo.type });

                 imagenesAdjuntas.push({ id: idName, archivo: nuevoArchivo });
             }

             mergeShapes(idName); // Llamamos a mergeShapes solo con el ID, sin la imagen

             return { idName, archivo };
         }
     });
 }




 function mergeShapes(idName){
   const canvas=document.createElement('canvas'); paper.setup(canvas);
   let paths=[];
   svg.querySelectorAll('rect,ellipse').forEach(el=>{
     let p;
     if(el.tagName==='rect'){
       const x=+el.getAttribute('x'),y=+el.getAttribute('y');
       const w=+el.getAttribute('width'),h=+el.getAttribute('height');
       p=new paper.Path.Rectangle(new paper.Rectangle(x,y,w,h));
     } else {
       const cx=+el.getAttribute('cx'),cy=+el.getAttribute('cy');
       const rx=+el.getAttribute('rx'),ry=+el.getAttribute('ry');
       p=new paper.Path.Ellipse({center:[cx,cy],radius:[rx,ry]});
     }
     paths.push(p);
   });
   if(paths.length===0) return;
   let result=paths[0]; for(let i=1;i<paths.length;i++) result=result.unite(paths[i]);
   const svgStr=result.exportSVG({asString:true});
   const newPath=new DOMParser().parseFromString(svgStr,'image/svg+xml').documentElement;
   newPath.setAttribute('fill','rgb(173,216,230)');
   newPath.setAttribute('stroke','none');
  if(idName) newPath.setAttribute('id', idName);
   clearHandles(); svg.querySelectorAll('rect,ellipse').forEach(el=>el.remove()); svg.appendChild(newPath);
   paper.project.clear(); lastMergedId=idName; updateButtonStyles();
 }



     // Enviar SVG al servidor
 async function sendSvgToServer() {
    //DMS validación del campo nombre
    const nombre = document.getElementById("nombre").value.trim();

     // ✅ Validar que el campo no esté vacío
     if (!nombre) {
         Swal.fire('Campo requerido', 'Por favor ingresa un nombre para el layout.', 'warning');
         return; // ⛔ Detiene la ejecución si está vacío
     }

//DMS esto parece que esta dando problemas cuando hablo en https con ec2 y cloudfront, mando ruta absoluta a ver que pasa
//     const endpoint = /*[[${#httpServletRequest.contextPath}]]*/ '' + '/guardarSvg';
     const endpoint = '/inicio/guardarSvg';
     const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
     const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
     const clone = svg.cloneNode(true);

     clone.querySelectorAll('path').forEach(path => {
         let id = path.getAttribute('id') || '';
        // id = toCamelCase(id);
         path.setAttribute("id", id);
         const anchor = document.createElementNS(svg.namespaceURI, 'a');
         anchor.setAttribute('href', `/activo/${toCamelCase(id)}`);
         path.parentNode.replaceChild(anchor, path);
         //DMS agrego data.estado
         path.setAttribute("data-estado", "operativa");

         anchor.appendChild(path);
     });
    //DMS elimino la proiedad fill antes de guardarlo en el servidor porquie me esta dando problemas con los parpadeos

    clone.querySelectorAll('path').forEach(path => path.removeAttribute('fill'));


     clone.querySelectorAll('image').forEach(img => {
         const xlinkHref = img.getAttributeNS("http://www.w3.org/1999/xlink", "href");
         if (xlinkHref) {
             img.removeAttributeNS("http://www.w3.org/1999/xlink", "href");
             img.setAttribute("href", xlinkHref);
         }
     });

    //DMS codigo insertado para hacer responsiva la imagen
    // Ajustar el SVG para hacerlo responsivo
    clone.removeAttribute("width");
    clone.removeAttribute("height");
    clone.setAttribute("viewBox", "0 0 900 600");
    clone.setAttribute("preserveAspectRatio", "xMidYMid meet");
    clone.style.width = "100vw";
    clone.style.height = "85vh";
    clone.style.display = "block";






     let source = new XMLSerializer().serializeToString(clone);

     try {
         console.log('Enviando SVG e imágenes a', endpoint);

         const nombre = document.getElementById("nombre").value.trim();
         //nombre=toCamelCase(nombre);
         const formData = new FormData();
         formData.append('svg', new Blob([source], { type: 'image/svg+xml' }));
         formData.append("nombre", nombre);
         // Agregar imágenes con sus nombres correctos dentro de la carpeta "imagenes"
         imagenesAdjuntas.forEach(item => {
             formData.append(item.id, item.archivo);
         });

         const response = await fetch(endpoint, {
             method: 'POST',
             headers: {
                 [csrfHeader]: csrfToken // Eliminamos 'Content-Type'
             },
             body: formData
         });

         console.log('Respuesta del servidor:', response.status);
         if (response.ok) {
           Swal.fire({
             title: 'Guardado',
             text: 'SVG e imágenes enviadas al servidor.',
             icon: 'success',
             confirmButtonText: 'Aceptar'
           }).then(() => {
             window.location.href = '/inicio/crearLayout';
           });
         } else {
           Swal.fire('Error', 'Ocurrió un problema al guardar.', 'error');
         }


     } catch (err) {
         console.error('Fetch falló:', err.name, err.message, err);

         Swal.fire('Error', 'No se pudo guardar.', 'error');
     }
 }




 function toCamelCase(str) {
     return str
        .normalize("NFD") // Descompone caracteres acentuados
        .replace(/[\u0300-\u036f]/g, '') // Elimina los acentos
        .replace(/[^a-zA-Z0-9\s]/g, '') // Mantiene números y elimina caracteres especiales
        .toLowerCase()
        .replace(/(?:^\w|[A-Z]|\b\w)/g, (word, index) =>
           index === 0 ? word.toLowerCase() : word.toUpperCase()
        )
        .replace(/\s+/g, '');
 }


//DMS ver para guardar una copia, asi como esta mete los paths dentro de una imagen
 /* function downloadSVG() {
       const serializer = new XMLSerializer();
       let source = serializer.serializeToString(svg);
       source = '<?xml version="1.0" standalone="no"?>\n' + '<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN"\n    "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">\n' + source;
       const blob = new Blob([source], { type: 'image/svg+xml;charset=utf-8' });
       const url = URL.createObjectURL(blob);
       const a = document.createElement('a');
       a.href = url;
       a.download = 'drawing.svg';
       a.click();
       URL.revokeObjectURL(url);
     }*/