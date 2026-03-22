package com.iffly.compose.markdown.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.MarkdownView
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.image.ImageMarkdownPlugin

@Composable
fun ImageExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        SelectionContainer {
            val markdownRenderConfig = remember {
                MarkdownRenderConfig.Builder()
                    .addPlugin(ImageMarkdownPlugin())
                    .build()
            }
            MarkdownView(
                text =
                    """
                    # Image and Media Example
                    
                    ![Image 4](https://b.zol-img.com.cn/soft/6/614/ceaszSDde0JHA.jpg)
                    
                    This example demonstrates image rendering functionality.
                    
                    ## Network Images
                    
                    ![Compose Logo](https://raw.githubusercontent.com/feiyin0719/AFreeSvg/dev/test.jpg)
                    
                    ## Image Description
                    
                    Above is the official Android robot image, showcasing network image loading capabilities.
                    
                    
                    ## Images Mixed with Text
                    
                    Text content can be nicely mixed with images. ![Small Icon](https://qcloud.dpfile.com/pc/w7BUcqbwgbqmYoDIbJcYkS-4p5gNsX7g5bXVyqeC386xoJR2wB3zvXKeaGZtgX19.jpg) Such small icons can be embedded within text.
                    
                    ## Multiple Images
                    
                    ![Image 1](https://photo.tuchong.com/24937277/f/725032695.jpg)
                    
                    ![Image 2](https://qcloud.dpfile.com/pc/w7BUcqbwgbqmYoDIbJcYkS-4p5gNsX7g5bXVyqeC386xoJR2wB3zvXKeaGZtgX19.jpg)
                    
                    ![Image 3](https://failed.test)
                    
                    The image above is clickable and will navigate to the Android developer website.
                    
                    ## Image Reference
                    
                    ![Image 5][id]
                    
                    [id]: https://qcloud.dpfile.com/pc/w7BUcqbwgbqmYoDIbJcYkS-4p5gNsX7g5bXVyqeC386xoJR2wB3zvXKeaGZtgX19.jpg
                    """.trimIndent(),
                modifier = Modifier.padding(16.dp),
                markdownRenderConfig = markdownRenderConfig,
            )
        }
    }
}
