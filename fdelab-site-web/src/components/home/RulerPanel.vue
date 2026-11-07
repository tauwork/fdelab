<template>
  <section id="ruler" class="ruler-wrap">
    <div class="ruler">
      <div class="ruler-head">
        <p class="kicker">价值标尺</p>
        <p class="q-title">项目结束时，<br>请用这三问验收我们。</p>
        <p class="q-sub">也欢迎用它验收任何 AI 落地项目——这把尺子本来就该在客户手里。</p>
        <p class="ruler-quote">「AI 时代企业最缺的不是模型，是有人帮它把业务判断翻译成机器可执行的标准。」</p>
      </div>
      <div class="ruler-body">
        <div class="qa">
          <span class="num">①</span>
          <div>
            <p class="q">你的业务判断，变成了可复用的标准吗？</p>
            <p class="a">留下的应该是 <b>Eval</b>——可版本化、可审计、可复用，不是一堆 prompt 和胶水代码。我们每个案例页都附「能力归属检验」，逐条对照给你看。</p>
          </div>
        </div>
        <div class="qa">
          <span class="num">②</span>
          <div>
            <p class="q">换个团队重做，要多久？</p>
            <p class="a">参照 OpenAI 半导体案例：首次部署 2 个月，第二个团队 <b>1 天</b>接手——定制代码删掉约 90%，剩下的就是标准。</p>
          </div>
        </div>
        <div class="qa">
          <span class="num">③</span>
          <div>
            <p class="q">模型升级，你是受益还是重做？</p>
            <p class="a">长在你侧的评测体系随模型升级<b>增值</b>；长在供应商手里的胶水代码，每次升级归零一次。</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.ruler-wrap {
  width: 100%;
  max-width: calc(var(--wrap) + 2 * var(--gut));
  margin-inline: auto;
  padding-inline: var(--gut);
  padding-block: 78px 10px;
}

.ruler {
  border-radius: 20px;
  padding: 46px 52px 44px;
  background: linear-gradient(120deg, #1b2440, #171c2e 62%);
  color: #fff;
  box-shadow: 0 30px 60px -34px rgba(23, 28, 46, .65);
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 330px) minmax(0, 1fr);
  gap: 0 64px;
  align-items: stretch;
}

.ruler::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 44% 38% at 88% 4%, rgba(90, 130, 255, .28), transparent 64%),
    radial-gradient(ellipse 40% 36% at 4% 100%, rgba(64, 98, 214, .24), transparent 66%);
}

.ruler > * {
  position: relative;
  z-index: 1;
}

.ruler-head {
  display: flex;
  flex-direction: column;
}

.kicker {
  color: var(--accent);
  letter-spacing: .3em;
  font-size: 11px;
  margin-bottom: 20px;
  font-weight: 500;
}

.ruler .kicker {
  color: #7ea6ff;
  margin-bottom: 16px;
}

.ruler .q-title {
  font-family: var(--serif);
  font-size: 31px;
  font-weight: 500;
  letter-spacing: .01em;
  line-height: 1.38;
}

.ruler .q-sub {
  font-size: 13.5px;
  color: rgba(255, 255, 255, .62);
  letter-spacing: .04em;
  margin-top: 16px;
  line-height: 1.85;
}

.qa {
  display: flex;
  gap: 22px;
  padding: 24px 0;
  border-top: 1px solid rgba(255, 255, 255, .12);
}

.ruler-body .qa:first-child {
  border-top: 0;
  padding-top: 2px;
}

.qa .num {
  font-family: var(--serif);
  font-size: 24px;
  color: #7ea6ff;
  width: 42px;
  flex-shrink: 0;
  line-height: 1.3;
}

.qa .q {
  font-family: var(--serif);
  font-size: 19px;
  font-weight: 500;
  line-height: 1.5;
  letter-spacing: .01em;
  margin-bottom: 10px;
}

.qa .a {
  font-size: 13px;
  line-height: 1.85;
  color: rgba(255, 255, 255, .68);
  max-width: none;
}

.qa .a b {
  color: #7ea6ff;
  font-weight: 500;
}

.ruler-quote {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, .12);
  font-family: var(--serif);
  font-size: 15.5px;
  font-weight: 500;
  line-height: 1.85;
  color: #bcd3ff;
}

@media (max-width: 1000px) {
  .ruler {
    grid-template-columns: 1fr;
    gap: 0;
    padding: 32px 28px 28px;
  }

  .ruler-head {
    margin-bottom: 26px;
  }

  .ruler .q-title {
    font-size: 24px;
  }

  .ruler-quote {
    margin-top: 24px;
  }

  .qa .q {
    font-size: 16px;
  }
}

@media (max-width: 640px) {
  .qa {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
