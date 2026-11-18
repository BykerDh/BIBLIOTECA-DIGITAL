import '../vendor/foliate/view.js';

const visor=document.querySelector('#visor');
const estado=document.querySelector('#lectura-estado');
const capitulos=document.querySelector('#capitulos');
const anterior=document.querySelector('#anterior');
const siguiente=document.querySelector('#siguiente');
const tamano=document.querySelector('#tamano');
const tema=document.querySelector('#tema');
const colores={claro:['#fffef9','#293b31'],sepia:['#f2e8d3','#4b3c2a'],noche:['#202c27','#e4e6db']};
const aplicarAspecto=()=>{
    const [fondo,tinta]=colores[tema.value]??colores.claro;
    const escala=['100','120','140'].includes(tamano.value)?tamano.value:'100';
    visor.style.background=fondo;
    visor.renderer.setStyles(`html,body{background:${fondo} !important;color:${tinta} !important;font-size:${escala}% !important;line-height:1.65 !important}p,div,span,h1,h2,h3,h4,li{color:inherit !important}a{color:inherit !important;text-decoration:underline}`);
};
const ejecutar=async operacion=>{try{await operacion();}catch(error){estado.hidden=false;estado.textContent='No se pudo cambiar de página. Intenta de nuevo o descarga el archivo.';console.error(error);}};
try {
    const respuesta=await fetch(visor.dataset.url,{credentials:'same-origin'});
    if(!respuesta.ok || respuesta.redirected) throw new Error('Acceso al archivo denegado');
    const archivo=new File([await respuesta.blob()],`libro.${visor.dataset.formato.toLowerCase()}`);
    visor.addEventListener('relocate',({detail})=>{document.querySelector('#progreso').textContent=`${Math.round((detail.fraction??0)*100)} %`;});
    visor.addEventListener('external-link',event=>event.preventDefault());
    await visor.open(archivo);
    visor.renderer.setAttribute('max-column-count','1');
    visor.renderer.setAttribute('max-inline-size','900px');
    visor.renderer.setAttribute('margin','24px');
    capitulos.replaceChildren();
    const agregar=(items,nivel=0)=>{for(const item of items??[]){if(item.href){const op=document.createElement('option');op.value=item.href;op.textContent='— '.repeat(nivel)+item.label;capitulos.append(op);}agregar(item.subitems,nivel+1);}};
    agregar(visor.book.toc);
    if(!capitulos.options.length){visor.book.sections.forEach((seccion,i)=>{const op=document.createElement('option');op.value=String(i);op.textContent=`Sección ${i+1}`;capitulos.append(op);});capitulos.dataset.indices='true';}
    capitulos.addEventListener('change',()=>ejecutar(()=>visor.goTo(capitulos.dataset.indices?Number(capitulos.value):capitulos.value)));
    anterior.addEventListener('click',()=>ejecutar(()=>visor.prev()));siguiente.addEventListener('click',()=>ejecutar(()=>visor.next()));
    tamano.addEventListener('change',aplicarAspecto);
    tema.addEventListener('change',aplicarAspecto);
    aplicarAspecto();
    await visor.init({showTextStart:true});
    estado.hidden=true;anterior.disabled=false;siguiente.disabled=false;capitulos.disabled=false;
    visor.dataset.cargado='true';
    window.addEventListener('pagehide',()=>visor.close(),{once:true});
} catch(error) {
    estado.textContent='No se pudo abrir el libro. Revisa que tu préstamo siga vigente. También puedes descargarlo para usar un lector compatible.';
    estado.classList.add('alert','error');console.error(error);
}
