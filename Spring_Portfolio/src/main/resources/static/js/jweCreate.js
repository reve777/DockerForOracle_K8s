$(document).ready(function () {
    const $form = $('#jweForm');
    const $pemRow = $('#pemRow');
    const $resultPre = $('#resultPre');
    const $pubPem = $('#pubPem');
    const $priPem = $('#priPem');
    const $licenseTxt = $('#licenseTxt');

    // 按鈕控制
    const $submitBtn = $('#submitBtn');
    const $printBtn = $('#printBtn');
    const $clearBtn = $('#clearBtn');
    const $downloadJweBtn = $('#downloadJweBtn');
    const $downloadLicBtn = $('#downloadLicBtn');
    const $uploadDefaultBtn = $('#uploadDefaultBtn');

    // 1. 切換 PEM 上傳顯示
    $('input[name="keyType"]').on('change', function () {
        if ($(this).val() === 'OGN') {
            $pemRow.fadeIn();
        } else {
            $pemRow.hide();
        }
    });

    // 2. 建立按鈕事件
    $submitBtn.on('click', function () {
        $resultPre.text('執行中...');
        $printBtn.hide();

        const fd = new FormData();
        fd.append('keyType', $('input[name="keyType"]:checked').val());
        fd.append('bankId', $('input[name="bankId"]').val() || '');
        fd.append('keyRef', $('input[name="keyRef"]').val() || '');
        fd.append('expireDate', $('input[name="expireDate"]').val() || '');

        const fileInput = $('input[name="file"]')[0];
        if (fileInput && fileInput.files.length > 0) {
            fd.append('file', fileInput.files[0]);
        }

        $.ajax({
            url: '/portfolio/jwekey/create',
            type: 'POST',
            data: fd,
            processData: false,
            contentType: false,
            success: function (json) {
                // 顯示原始 JSON
                $resultPre.text(JSON.stringify(json, null, 2));
                $printBtn.show();

                // 💡 修正路徑：根據 JSON 結構，資料在 json.data.data
                const innerData = (json.data && json.data.data) ? json.data.data : null;

                if (innerData) {
                    // 公鑰解析 (pubKeyBase64)
                    const pub = innerData.pubKeyBase64;
                    if (pub) {
                        const pem = '-----BEGIN PUBLIC KEY-----\n' + chunkString(pub, 64) + '\n-----END PUBLIC KEY-----\n';
                        $pubPem.text(pem);
                    }

                    // 私鑰解析 (priKeyBase64)
                    const pri = innerData.priKeyBase64;
                    if (pri) {
                        const pemPri = '-----BEGIN PRIVATE KEY-----\n' + chunkString(pri, 64) + '\n-----END PRIVATE KEY-----\n';
                        $priPem.text(pemPri);
                    }

                    // 更新隱藏的 License 欄位（供下載用）
                    $licenseTxt.val(innerData.license || "");
                    
                    $downloadJweBtn.show();
                    $downloadLicBtn.show();
                }
            },
            error: function (xhr) {
                let errorMsg = "失敗: ";
                try {
                    const errObj = JSON.parse(xhr.responseText);
                    errorMsg += errObj.message || xhr.statusText;
                } catch (e) {
                    errorMsg += xhr.statusText;
                }
                $resultPre.text(errorMsg);
            }
        });
    });

    // 3. 清除按鈕
    $clearBtn.on('click', function () {
        $form[0].reset();
        $resultPre.text('');
        $pubPem.text('');
        $priPem.text('');
        $licenseTxt.val('');
        $pemRow.hide();
        $('.btn-hidden').hide();
    });

    // 4. 列印
    $printBtn.on('click', function () {
        const w = window.open('', '_blank');
        w.document.write('<pre>' + $resultPre.text().replace(/</g, '&lt;') + '</pre>');
        w.document.close();
        w.print();
    });

    // 5. 下載 JWE
    $downloadJweBtn.on('click', function() {
        const content = $pubPem.text();
        if (!content) return alert('無公鑰可下載');
        downloadFile(content, 'public_key.pem', 'application/x-pem-file');
    });

    // 6. 下載 License
    $downloadLicBtn.on('click', function() {
        const content = $licenseTxt.val();
        if (!content) return alert('無內容可下載');
        downloadFile(content, 'demo.license', 'text/plain');
    });

    // 7. 載入預設檔案 (前端輔助)
    $uploadDefaultBtn.on('click', function() {
        $.get('/portfolio/sample/default_pub.pem', function(text) {
            const blob = new Blob([text], { type: 'application/x-pem-file' });
            const dt = new DataTransfer();
            dt.items.add(new File([blob], 'default_pub.pem'));
            $('input[name="file"]')[0].files = dt.files;
            alert('已置入預設檔案');
        }).fail(function() {
            alert('無法取得預設檔案，請確認路徑');
        });
    });

    function downloadFile(content, fileName, mimeType) {
        const blob = new Blob([content], { type: mimeType });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();
        URL.revokeObjectURL(url);
    }

    function chunkString(str, length) {
        if (!str) return '';
        const chunks = [];
        for (let i = 0; i < str.length; i += length) {
            chunks.push(str.substring(i, i + length));
        }
        return chunks.join('\n');
    }
});