'use strict'

const {dialog} = require('electron').remote
const fs = require('fs')
const { ipcRenderer } = require('electron')
var path = require('path')
const ExcelJS = require('exceljs');


document.getElementById('Refresh').addEventListener('click', () => {
  ipcRenderer.send('maj')
})

// Lorsque l'on clique sur DownloadBtn le renderer process envoie au main process json (cf main.js)
document.getElementById('DownloadBtn').addEventListener('click', () => {
  alert('JSON téléchargé')
  ipcRenderer.send('json')
})

// Lorsque l'on clique sur clearBtn le renderer process envoie au main process clear-txt (cf main.js)
document.getElementById('clearBtn').addEventListener('click', () => {
    alert("Texte effacé")
    ipcRenderer.send('clear-txt')
  })

// Lorsque l'on clique sur AnnoterBtn le renderer process envoie au main process add-ann-window
// qui sera la fenêtre pour annoter
document.getElementById('AnnoterBtn').addEventListener('click', () => {
    ipcRenderer.send('add-ann-window')
  })

/* Annotation d'une partie de texte */
document.getElementById('AnnoterPartBtn').addEventListener('click', () => {

  console.log(ipcRenderer.send('add-ann-specifique-window'))

  })

// Lorsque l'on clique sur AnnoterBtn le renderer process envoie au main process add-window
// qui sera la fenêtre pour écrire notre texte
document.getElementById('WriteBtn').addEventListener('click', () => {
  ipcRenderer.send('add-window')
})

    // Lorsque l'on clique sur AddtxtBtn le renderer process envoie au main process add-txt
// qui permet d'importer du texte
document.getElementById('AddtxtBtn').addEventListener('click', () => {
  dialog.showOpenDialog((fileNames) => {
    if(fileNames === undefined){
      console.log('No file was selected')
    }else {
      var filePath = String(fileNames)
      var ext = path.extname(filePath)
      if (ext == '.txt'){
        fs.readFile(fileNames[0], 'utf-8', (err, data) => {
          if (err){
            console.log('cannot read file', err)
          }else{
            ipcRenderer.send('add-txt', data)
          }
        })
      }else if(ext == '.json'){
        ipcRenderer.send('add-json-window')
        ipcRenderer.once('key-json', (event, key) => {
          fs.readFile(fileNames[0], 'utf-8', (err, data) => {
            if (err){
              console.log('cannot read file', err)
            }else{
              const obj = data.split('{')
              for (var i = 1; i<obj.length; i++){
                const text = obj[i].split('\'' + key + '\'')
                const txt = text[1].split('\'')
                ipcRenderer.send('add-txt', txt[1])
              }
            }
          })
        })
      }else if (ext == '.csv'){
        ipcRenderer.send('add-csv-window')
        ipcRenderer.once('key-csv', (event, key_sep) => {
          fs.readFile(fileNames[0], 'utf-8', (err, data) => {
            if (err){
              console.log('cannot read file', err)
            }else{
              const key = key_sep.split(';')[0]
              const sep= key_sep.split(';')[1]
              const obj = data.split('\n')
              var ind = 0
              const index = obj[0].split(',')
              for (var j = 0; j<index.length; j++){
                if (index[j] == key){
                  ind = j;
                }
              }
              for (var i = 1; i<obj.length-1; i++){
                const text = obj[i].split(sep)
                ipcRenderer.send('add-txt', text[ind])
              }
            }
          })
        })
      }else if (ext == '.xlsx'){
        ipcRenderer.send('add-json-window')
        ipcRenderer.once('key-json', (event, key) => {
          const workBook = new ExcelJS.Workbook()
          workBook.xlsx.readFile(filePath).then(function() {
            const sheet = workBook.getWorksheet(1)
            var print = 0
            for (var i = 1; i<= sheet.columnCount; i++){
              const col = sheet.getColumn(i)
              col.eachCell({ includeEmpty: false }, function(cell, rowNumber) {
                if (rowNumber == 1){
                  print = 0
                  if(cell.value ==key){
                    print = 1
                  }
                }else {
                  if (print == 1){
                    ipcRenderer.send('add-txt', cell.value)
                  }
                }
              })
            }
          })
        })
      }
    }
  })
})



// Quand ce renderer process reçoit inputstoPrint
// il va ajouter le contenu du JSON file dans la page html ann_menu.html
// dans la balise id=Inputtxt
ipcRenderer.on('inputstoPrint', (event, txt) => {
      // get the Inputtxt id=Inputtxt
      const Inputtxt = document.getElementById('Inputtxt')

      // create html string
      const txtItems = txt.reduce((html, text) => {
        html += `<a id="input" class="input-txt">${text}</a>`
        //html += `<textarea id="input" class="input-txt" readonly>${text}</textarea>`
        return html
      }, '')

      // set list html to the txtitems
      Inputtxt.innerHTML = txtItems

    })

ipcRenderer.on('toClear', (event) => {
    var list = document.getElementsByClassName("input-txt");
    console.log(list)
    for(var i = list.length-1; i=>0; i--){
      list[i].parentElement.removeChild(list[i]);
    }
  })
